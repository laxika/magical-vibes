#!/usr/bin/env python3
"""Step 15, run 17u: exact doc edits for the run's authoring changes.

- "Its controller loses N life" of an enchanted creature that attacks, blocks or is dealt damage, and "that player loses
  N life" at the upkeep of the enchanted creature's controller, are `LoseLifeEffect` (TRIGGERING_PERMANENT_CONTROLLER /
  TRIGGERING_PLAYER); `EnchantedCreatureControllerLosesLifeEffect` is left for the "when enchanted creature dies"
  collector (Banewasp Affliction, Decomposition).
- `RemoveTimeCounterWhenOpponentCastsSpellEffect` is gone (Deep-Sea Kraken: an intervening "if" and
  `RemoveTimeCounterFromExiledCardEffect`).
- `MustBlockSourceEffect.ofTriggeringPermanent()` (Grappling Hook); no collector rebinds the source.
- A triggered "choose one at random" (`RandomChoiceEffect`) is chosen as the ability goes on the stack (CR 603.3c).
- `ChooseCardsFromTargetHandEffect.ofTriggeringPlayer()` (Leshrac's Sigil's "that player's hand").

Fails if a doc still names the removed class. Idempotent.

Usage: step15-run17u-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `RemoveTimeCounterWhenOpponentCastsSpellEffect` | `()` | suspended-card `Triggers.opponentCastsSpell()` "
         "trigger descriptor: queues `RemoveTimeCounterFromExiledCardEffect` for the source card when an opponent casts "
         "a spell; the exiled-card trigger scan requires a positive time-counter entry. Used by Deep-Sea Kraken |\n", ""),
        ("| `RandomChoiceEffect` | `(List<List<CardEffect>> options)` | selects one non-empty effect sequence uniformly "
         "at random at resolution and splices the selected steps into the live stack entry. Used by Typhoid Mary, "
         "Fractured |",
         "| `RandomChoiceEffect` | `(List<List<CardEffect>> options)` | \"choose one at random\": selects one non-empty "
         "effect sequence uniformly at random. A triggered ability's mode is chosen as it goes on the stack (CR 603.3c, "
         "`StackedModalTriggerService`), so the entry and its copies carry the chosen steps; otherwise it is chosen at "
         "resolution. Used by Typhoid Mary, Fractured; Cult of Skaro |"),
        ("| `RandomChoiceEffect` | `(List<CardEffect> options)` | resolves exactly one supplied effect selected uniformly "
         "at random at resolution; general-purpose random alternative selection for non-token effects. Used by Cult of "
         "Skaro |\n", ""),
        ("| `RandomChoiceEffect` | `(List<List<CardEffect>> options)` | selects one supplied effect sequence uniformly at "
         "random at resolution and splices its steps into the live stack entry. Used by Typhoid Mary, Fractured |",
         "| `RandomChoiceEffect` | `(List<List<CardEffect>> options)` | \"choose one at random\": one supplied effect "
         "sequence, selected uniformly at random; a triggered ability's mode is chosen as it goes on the stack "
         "(CR 603.3c). Used by Cult of Skaro, Typhoid Mary, Fractured |"),
        ("| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | enchanted creature's "
         "controller loses N life (trigger). Fixed-amount upkeep form is Numbing Dose. On the",
         "| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | **Only** \"when "
         "enchanted creature dies, its controller loses …\" (left for step 15's enchanted-death row): an enchanted "
         "creature that attacks, blocks or is dealt damage is `LoseLifeEffect(N or EventValue, "
         "TRIGGERING_PERMANENT_CONTROLLER)` (Contaminated Bond, Ragged Veins), the upkeep of its controller "
         "`LoseLifeEffect(N, TRIGGERING_PLAYER)` (Numbing Dose). On the"),
        ("| `MustBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature must block source permanent this turn if "
         "able.",
         "| `MustBlockSourceEffect` | `(UUID sourcePermanentId)` / `.ofTriggeringPermanent()` | target creature must block "
         "source permanent this turn if able; `ofTriggeringPermanent()` blocks the stack entry's triggering permanent "
         "instead (an Equipment's \"whenever equipped creature attacks, you may have target creature block it\", "
         "Grappling Hook)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `RemoveTimeCounterWhenOpponentCastsSpellEffect()` - suspended-card trigger descriptor: when an opponent casts "
         "a spell, queue `RemoveTimeCounterFromExiledCardEffect` for this card; only fires while the card has a time "
         "counter in exile (Deep-Sea Kraken)\n", ""),
        ("Default constructors retain the turn-long requirement. Block validation",
         "Default constructors retain the turn-long requirement; `MustBlockSourceEffect.ofTriggeringPermanent()` makes "
         "the creature block the stack entry's triggering permanent (Grappling Hook's equipped creature). Block "
         "validation"),
        ("- `RandomChoiceEffect(List<CardEffect>)` — resolve exactly one supplied effect, selected uniformly at random at "
         "resolution; use when the random alternatives are not limited to token creation (Cult of Skaro)\n", ""),
        ("- `RandomChoiceEffect(List<List<CardEffect>>)` — select one supplied effect sequence uniformly at random at "
         "resolution and splice its steps into the live stack entry (Typhoid Mary, Fractured)",
         "- `RandomChoiceEffect(List<List<CardEffect>>)` — \"choose one at random\": one supplied effect sequence, "
         "selected uniformly at random; a triggered ability's mode is chosen as it goes on the stack (CR 603.3c), so its "
         "copies keep it (Cult of Skaro, Typhoid Mary, Fractured)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `RemoveTimeCounterWhenOpponentCastsSpellEffect()` + `RemoveTimeCounterFromExiledCardEffect(UUID)` | "
         "Triggers.opponentCastsSpell() while suspended | Deep-Sea Kraken. The spell-cast trigger is collected from the "
         "suspended card in exile;",
         "| `TriggerOptions.DEFAULT.withInterveningIf(new SourceCardSuspended())` + "
         "`RemoveTimeCounterFromExiledCardEffect(getId())` | Triggers.opponentCastsSpell() | Deep-Sea Kraken (as Pardic "
         "Dragon writes its ability). The suspended reader collects the ability from the suspended card in exile;"),
        ("`new MayPayManaEffect(\"{B}{B}\", new ChooseCardsFromTargetHandEffect(1, List.of(), DISCARD), \"prompt\")` | "
         "`Triggers.opponentCastsSpell()` | Leshrac's Sigil. Generic collector stamps casting opponent as non-targeting "
         "`targetId` (\"that player\");",
         "`new MayPayManaEffect(\"{B}{B}\", ChooseCardsFromTargetHandEffect.lookAtTargetHand(new Fixed(1), DISCARD)"
         ".ofTriggeringPlayer(), \"prompt\")` | `Triggers.opponentCastsSpell()` | Leshrac's Sigil. `ofTriggeringPlayer()` "
         "declares no target; the generic collector stamps the casting opponent as non-targeting `targetId` "
         "(\"that player\");"),
        ("The effect option is selected uniformly at random as the trigger resolves;",
         "The mode is chosen uniformly at random as the ability goes on the stack (CR 603.3c);"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("(fires regardless of which player controls the aura). `affectedPlayerId` is baked in at trigger time for "
         "effects like `EnchantedCreatureControllerLosesLifeEffect` |",
         "(fires regardless of which player controls the aura). \"That player\" is the entry's non-targeting target: "
         "`LoseLifeEffect(N, TRIGGERING_PLAYER)`, `DealDamageToPlayersEffect(N, ENCHANTED_PERMANENT_CONTROLLER)` |"),
        ("`EnchantedCreatureControllerLosesLifeEffect(0)` (Ragged Veins — life loss equal to the damage) |",
         "`LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` (Ragged Veins — life loss equal to the "
         "damage) |"),
    ],
    "ARCHITECTURE.md": [
        ("a lone plain `MayEffect` / `MayPayManaEffect` on the battlefield or in exile as the optional part the collector "
         "asks on resolution (CR 603.5) after choosing targets (CR 603.3d),",
         "a lone plain `MayEffect` / `MayPayManaEffect` put on the stack as the card wrote it and asked as it resolves "
         "(CR 603.5), the targets of what it offers chosen as the ability goes on the stack "
         "(`CastAbility.effectsTargetsAreChosenFor()`, CR 603.3d),"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER) EnchantedCreatureControllerLosesLifeEffect(1)",
         "Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER) LoseLifeEffect(1, TRIGGERING_PLAYER)"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        original = text
        for old, new in edits:
            if old in text:
                text = text.replace(old, new)
            elif new and new not in text:
                raise SystemExit(f"{path}: expected text not found: {old[:100]!r}")
        if text != original:
            path.write_text(text)
            print(f"edited {path}")
    for path in DOCS.glob("*.md"):
        if "RemoveTimeCounterWhenOpponentCastsSpellEffect" in path.read_text():
            raise SystemExit(f"{path} still names RemoveTimeCounterWhenOpponentCastsSpellEffect")


if __name__ == "__main__":
    main()
    sys.exit(0)
