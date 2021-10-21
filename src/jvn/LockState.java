package jvn;

public enum LockState {
    NL,      // No Lock
    RLC,     // Read Lock Cached
    WLC,     // Write Lock Cached
    RLT,     // Read Lock Taken
    WLT,     // Write Lock Taken
    RLT_WLC, // Read Lock Taken - Write Lock Cached
}
