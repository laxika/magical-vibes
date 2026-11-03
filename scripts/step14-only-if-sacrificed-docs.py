#!/usr/bin/env python3
"""Effect-slot refactor step 14 run 16a: agent-docs passages about the removed `onlyIfSacrificed` flags."""
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EDITS = {
    'agent-docs/EFFECTS_QUICK_REFERENCE.md': [
        ("; `sacrificeOnly(int)` is the `Triggers.sacrificed(THIS)` form for \"when you sacrifice this, it deals N damage to any target\" and skips non-sacrifice deaths",
         "; \"when you sacrifice this, it deals N damage to any target\" is `Triggers.sacrificed(THIS)` with the plain effect"),
        ("`DiscardEffect(DynamicAmount, DiscardRecipient, boolean random[, CardType stopAfterDiscardingType, boolean onlyIfSacrificed])` — the whole discard family; `recipient` routes who discards, `random` picks chosen vs random discard, and `onlyIfSacrificed=true` makes a `Triggers.sacrificed(THIS)` effect trigger only when its source was sacrificed. `DiscardEffect.sacrificeOnly(N)` is the target-player form for \"when you sacrifice this, target player discards N cards.\"",
         "`DiscardEffect(DynamicAmount, DiscardRecipient, boolean random[, CardType stopAfterDiscardingType])` — the whole discard family; `recipient` routes who discards, `random` picks chosen vs random discard. \"When you sacrifice this, target player discards N cards\" is `Triggers.sacrificed(THIS)` + `new DiscardEffect(N, DiscardRecipient.TARGET_PLAYER)`."),
        ("`GainLifeEffect.sacrificeOnly(N)` is the `Triggers.sacrificed(THIS)` form of \"when you sacrifice this, gain N life\".",
         "\"When you sacrifice this, gain N life\" is `Triggers.sacrificed(THIS)` + `new GainLifeEffect(N)`."),
    ],
    'agent-docs/ORACLE_TEXT_EFFECT_MAP.md': [
        ("`RegisterDelayedReturnSourceTransformedEffect(true, true)` (onlyIfSacrificed + underOwnerControl). Collected from the sacrifice path via `CardEffect.onlyTriggersOnSacrifice()`, not on other deaths.",
         "`RegisterDelayedReturnSourceTransformedEffect(true)` (underOwnerControl). The trigger's sacrifice cause keeps it from triggering on other deaths."),
        ("`Triggers.sacrificed(THIS)` + `DealDamageToAnyTargetEffect.sacrificeOnly(N)`",
         "`Triggers.sacrificed(THIS)` + `new DealDamageToAnyTargetEffect(N)`"),
    ],
    'agent-docs/EFFECTS_INDEX.md': [
        ("`(DynamicAmount, Condition unpreventableWhen)`; `sacrificeOnly(int)` ", "`(DynamicAmount, Condition unpreventableWhen)` "),
        (" `sacrificeOnly(int)` marks a `Triggers.sacrificed(THIS)` effect to trigger only when its source was sacrificed.", ""),
        ("| `DrawCardEffect` | `(DynamicAmount amount, boolean onlyIfSacrificed)` |", "| `DrawCardEffect` | `(DynamicAmount amount)` |"),
        (", `sacrificeOnly(int amount)` for \"when you sacrifice this, draw …\" death triggers.", "; \"when you sacrifice this, draw …\" is `Triggers.sacrificed(THIS)` with the plain effect."),
        ("`(DynamicAmount amount, DiscardRecipient recipient, boolean random[, CardType stopAfterDiscardingType, boolean onlyIfSacrificed])`",
         "`(DynamicAmount amount, DiscardRecipient recipient, boolean random[, CardType stopAfterDiscardingType])`"),
        (" `onlyIfSacrificed=true` filters a `Triggers.sacrificed(THIS)` registration to the sacrifice path; `DiscardEffect.sacrificeOnly(N)` is the target-player form for \"when you sacrifice this, target player discards N cards",
         " \"When you sacrifice this, target player discards N cards\" is `Triggers.sacrificed(THIS)` + `new DiscardEffect(N, DiscardRecipient.TARGET_PLAYER)`"),
    ],
}

def main():
    dry = '--dry-run' in sys.argv
    for rel, edits in EDITS.items():
        path = os.path.join(ROOT, rel)
        s = open(path).read()
        for old, new in edits:
            if old not in s:
                if new and new in s:
                    continue
                print(f'MISSING in {rel}: {old[:90]!r}')
                continue
            s = s.replace(old, new, 1)
        if not dry:
            open(path, 'w').write(s)

if __name__ == '__main__':
    main()
