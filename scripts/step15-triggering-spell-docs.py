#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 14: docs and skills for the triggering spell on the stack entry.

Idempotent: python3 scripts/step15-triggering-spell-docs.py

Exact edits in agent-docs/ (EFFECTS_INDEX.md, EFFECTS_QUICK_REFERENCE.md, ORACLE_TEXT_EFFECT_MAP.md,
CARD_PATTERNS_CREATURES_TRIGGERED.md) for the effects that lost their bound spell data, the three removed classes,
Bill Potts and CopyTriggeringSpellOrAbilityEffect, and the removal of OncePerTurnTriggerEffect from the implement-card
and review-card skills (the class is gone, so no card can author it).
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent

WRAPPER = "`OncePerTurnTriggerEffect`"
SKILL_EDITS = [
    (f"in {WRAPPER} or an intervening-if", "in an intervening-if"),
    (f"never a {WRAPPER} / intervening-if", "never an intervening-if"),
    (f"never `ConditionalEffect`, {WRAPPER} around", "never `ConditionalEffect` around"),
    (f"`AbilityTargetChoiceTriggerEffect`, {WRAPPER} or an intervening-if",
     "`AbilityTargetChoiceTriggerEffect` or an intervening-if"),
    (f"; never write {WRAPPER} around a library ability's effects.",
     "; never write a wrapper effect around a library ability's effects."),
    (f"never write {WRAPPER} or an intervening-if", "never write an intervening-if"),
    (f"Never write {WRAPPER}, `FirstOpponentLifeLossEachTurnTriggerEffect` or",
     "Never write `FirstOpponentLifeLossEachTurnTriggerEffect` or"),
    (f", never {WRAPPER}. A chapter takes", ". A chapter takes"),
    (f"never {WRAPPER} / `ConditionalEffect` around", "never `ConditionalEffect` around"),
    (f"`becomesSaddledForFirstTimeEachTurn()`, never {WRAPPER}),", "`becomesSaddledForFirstTimeEachTurn()`),"),
    (f" / a wrapper effect / {WRAPPER} around", " / a wrapper effect around"),
    (f"never a wrapper effect / {WRAPPER} / intervening-if", "never a wrapper effect / intervening-if"),
    (f"(flag a `ConditionalEffect`, {WRAPPER} or `Triggering…ConditionalEffect`",
     "(flag a `ConditionalEffect` or `Triggering…ConditionalEffect`"),
    (f"never {WRAPPER} / a wrapper effect /", "never a wrapper effect /"),
    (f"/ a wrapper effect / {WRAPPER} / intervening-if", "/ a wrapper effect / intervening-if"),
    (f"and never {WRAPPER} / intervening-if", "and never intervening-if"),
    (f"never {WRAPPER} / `FirstOpponentLifeLossEachTurnTriggerEffect`",
     "never `FirstOpponentLifeLossEachTurnTriggerEffect`"),
    (f"`TriggerLimit.ONCE_EACH_TURN`, never {WRAPPER}; a chapter", "`TriggerLimit.ONCE_EACH_TURN`; a chapter"),
    (f"never {WRAPPER} / `StateTriggerEffect`", "never `StateTriggerEffect`"),
    (f"never a wrapper effect / {WRAPPER} wrappers", "never wrapper effects"),
]

DOC_EDITS = {
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        (" `OncePerTurnTriggerEffect(CardEffect)` is left only on Bill Potts, whose one ability is two trigger conditions"
         " with two event-binding effects (step 15); don't author it.",
         " One ability of a cast and an activation condition shares one limit too: Bill Potts' \"whenever you cast an"
         " instant or sorcery spell that targets only Bill Potts or activate an ability that targets only Bill Potts,"
         " copy that spell or ability\" is `addTrigger(List.of(Triggers.youCastSpell().withFilter(instantOrSorcery)"
         ".withStackEntryFilter(targetsOnlyBill), Triggers.youActivateAbility().withStackEntryFilter(targetsOnlyBill)),"
         " TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new CopyTriggeringSpellOrAbilityEffect())`."),
        ("- `PutCountersOnSelfEqualToTriggeringSpellManaValueEffect(CounterType)` - spell-cast trigger payload that puts"
         " counters on the source permanent equal to the triggering spell's mana value; the generic spell-cast collector"
         " snapshots that value into the trigger entry (Geometric Nexus, C21 #77)",
         "- \"Put counters on this equal to that spell's mana value\" is `PutCountersOnSelfEffect(CounterType,"
         " new TargetSpellManaValue())`: `TargetSpellManaValue` reads the triggering spell on the stack, or the stack"
         " entry's triggering spell once it has left (Geometric Nexus, C21 #77)"),
        ("- `ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect(CardPredicate,"
         " GraveyardSearchScope)` — trigger-time descriptor that snapshots the triggering spell's mana value into an"
         " opponent-graveyard card filter, then reuses the free copy-cast flow",
         "- \"Exile target … card with equal or lesser mana value than that spell from an opponent's graveyard\" in a"
         " reflexive ability is `QueueReflexiveAbilityEffect(new ExileTargetCardFromGraveyardAndMayCastCopyEffect("
         "AllOf(filter, new CardMaxManaValueXPredicate()), OPPONENT_GRAVEYARD), false, true)`: the reflexive graveyard"
         " target choice's X is the entry's event value, the triggering spell's mana value (Saruman of Many Colors)"),
        ("- `AmassGoblinsEqualToCastSpellManaValueEffect([CardSubtype subtype])` — Triggers.youCastSpell() trigger"
         " descriptor: amass the chosen subtype with counters equal to the triggering spell's full mana value; snapshots"
         " into `AmassGoblinsEffect` so the normal amass resolution path is reused. Saruman, the White Hand",
         "- \"Amass Orcs X, where X is that spell's mana value\" is `AmassGoblinsEffect(new TargetSpellManaValue(),"
         " CardSubtype.ORC)` on the cast trigger. Saruman, the White Hand"),
        ("- `LoseLifeToOwnerOfTriggeringSpellEffect()` — spell-cast trigger payload that snapshots the triggering spell's"
         " mana value and makes that spell's owner lose that much life (The Ruinous Powers)",
         "- `LoseLifeToOwnerOfTriggeringSpellEffect()` — spell-cast trigger payload that makes that spell's owner lose"
         " life equal to its mana value, the stack entry's event value (The Ruinous Powers)"),
        ("- `CopyTriggeringSpellEffect([boolean tokenCopy[, boolean retargetToSource]])` — copies the spell that caused"
         " the resolving spell-cast trigger;",
         "- `CopyTriggeringSpellOrAbilityEffect()` — \"copy that spell or ability\" (Bill Potts): copies the stack entry's"
         " triggering spell, or the activated ability an activation trigger was about (with"
         " `AbilityTrigger.withStackEntryFilter` judged once the ability is on the stack); the copy's controller may"
         " choose new targets\n"
         "- `CopyTriggeringSpellEffect([boolean tokenCopy[, boolean retargetToSource]])` — copies the spell that caused"
         " the resolving spell-cast trigger (the stack entry's triggering spell, as it was when the ability triggered,"
         " once it has left the stack);"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `OncePerTurnTriggerEffect` | `(CardEffect wrapped)` | Bill Potts only: its \"this ability triggers only once"
         " each turn\" covers a cast trigger and an activation trigger whose effects are two event bindings, so the source"
         " is marked when either fires (step 15 makes it one ability). Don't author it: \"triggers only once each turn\""
         " is `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`, \"do this only once each turn\""
         " `TriggerLimit.DO_ONLY_ONCE_EACH_TURN`, \"for the first time each turn\" `TriggerLimit.FIRST_TIME_EACH_TURN` |",
         "| `CopyTriggeringSpellOrAbilityEffect` | `()` | \"copy that spell or ability. You may choose new targets for the"
         " copy\" (Bill Potts, one ability of a cast and an activation condition with"
         " `TriggerLimit.ONCE_EACH_TURN`): copies the stack entry's triggering spell, or the activated ability an"
         " activation trigger was about (`StackEntry.getTriggeringActivatedAbility()`); an activation condition's"
         " `AbilityTrigger.withStackEntryFilter` is judged once the ability is on the stack |"),
        ("| `PutCountersOnSelfEqualToTriggeringSpellManaValueEffect` | `(CounterType counterType)` | spell-cast trigger"
         " payload: puts counters on the source permanent equal to the triggering spell's mana value, using the"
         " cast-time snapshot carried by the trigger entry. Used by Geometric Nexus (C21 #77) |\n", ""),
        ("| `ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect` | `(CardPredicate"
         " filter, GraveyardSearchScope scope)` | trigger-only descriptor that snapshots the triggering spell's mana"
         " value, combines it with `filter`, and reuses the free `ExileTargetCardFromGraveyardAndMayCastCopyEffect`"
         " flow |\n", ""),
        ("| `AmassGoblinsEqualToCastSpellManaValueEffect` | `([CardSubtype subtype])` | `Triggers.youCastSpell()` trigger"
         " descriptor: amass the chosen subtype with counters equal to the triggering spell's full mana value; the"
         " trigger collector snapshots into `AmassGoblinsEffect` so ordinary amass resolution handles Army creation,"
         " selection, counters, and subtype grant. Saruman, the White Hand |\n", ""),
        ("| `LoseLifeToOwnerOfTriggeringSpellEffect` | `(int amount)` | makes the owner of the spell that caused the"
         " trigger lose the supplied amount; snapshots the triggering spell's mana value for spell-cast triggers. Used"
         " by The Ruinous Powers |",
         "| `LoseLifeToOwnerOfTriggeringSpellEffect` | `()` | makes the owner of the spell that caused the trigger lose"
         " life equal to its mana value, the stack entry's event value. Used by The Ruinous Powers |"),
        ("| `DemonstrateEffect` | `([StackEntry spellSnapshot])` | trigger descriptor for the"
         " `Triggers.youCastThisSpell()` ability, normally wrapped in `MayEffect`: snapshots the triggering spell so it"
         " can still be copied after leaving the stack;",
         "| `DemonstrateEffect` | `()` | trigger descriptor for the `Triggers.youCastThisSpell()` ability, normally"
         " wrapped in `MayEffect`: copies the stack entry's triggering spell, so it can still be copied after leaving"
         " the stack;"),
        ("`GrantProtectionFromTriggeringSpellColorsEffect()` is a non-targeting source effect for spell-cast triggers."
         " The spell-cast collector snapshots the triggering spell's colors into the effect, including through"
         " conditional, sequence, and may wrappers, so it still grants a permanent floating `ProtectionFromColorsEffect`"
         " if that spell has left the stack.",
         "`GrantProtectionFromTriggeringSpellColorsEffect()` is a non-targeting source effect for spell-cast triggers."
         " Once the triggering spell has left the stack it reads that spell's last-known colors, falling back to the"
         " stack entry's triggering spell (the spell as the ability triggered), so it still grants a permanent floating"
         " `ProtectionFromColorsEffect`."),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`CascadeEffect` implements `TriggeringSpellManaValueEffect`, so the generic spell-cast collector carries the"
         " triggering spell's announced mana value into `CascadeEffectHandler`",
         "`CascadeEffect` implements `TriggeringSpellManaValueEffect`, so the generic spell-cast collector sets the"
         " stack entry's event value to the triggering spell's announced mana value for `CascadeEffectHandler`"),
        ("with `AmassGoblinsEqualToCastSpellManaValueEffect(ORC)` | `Triggers.youCastSpell()` | Saruman, the White Hand"
         " (LTC 8). The trigger snapshots the spell's full mana value into the existing amass resolution path;",
         "with `AmassGoblinsEffect(new TargetSpellManaValue(), CardSubtype.ORC)` | `Triggers.youCastSpell()` | Saruman,"
         " the White Hand (LTC 8). `TargetSpellManaValue` is the spell's full mana value, read from the stack entry's"
         " triggering spell once it has left the stack;"),
        ("| `SpellCastTriggerEffect.nth(2, null, List.of(MillEachOpponentThenIfMilledEffect(2,"
         " ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect(CardAnyOfPredicate("
         "ENCHANTMENT, INSTANT, SORCERY), OPPONENT_GRAVEYARD)))` | Triggers.youCastSpell() | Saruman of Many Colors."
         " The graveyard target is chosen as the second-spell trigger is put on the stack;",
         "| `Triggers.youCastSpell().nthThisTurn(2)` with `MillEachOpponentThenIfMilledEffect(2,"
         " new QueueReflexiveAbilityEffect(new ExileTargetCardFromGraveyardAndMayCastCopyEffect(AllOf(AnyOf(ENCHANTMENT,"
         " INSTANT, SORCERY), new CardMaxManaValueXPredicate()), OPPONENT_GRAVEYARD), false, true))` |"
         " Triggers.youCastSpell() | Saruman of Many Colors. The graveyard target is chosen as the reflexive ability"
         " is put on the stack, with the triggering spell's mana value (the entry's event value) as its X;"),
        ("+ `new PutCountersOnSelfEqualToTriggeringSpellManaValueEffect(CHARGE)` +",
         "+ `new PutCountersOnSelfEffect(CHARGE, new TargetSpellManaValue())` +"),
        ("the collector snapshots the triggering spell's colors, retaining them if the spell leaves before the trigger"
         " resolves.",
         "the effect reads the stack entry's triggering spell, so it keeps the spell's colors if the spell leaves before"
         " the trigger resolves."),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`MillEachOpponentThenIfMilledEffect(2, ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpell"
         "ManaValueEffect(...))` — choose the graveyard target when the trigger is created;",
         "`MillEachOpponentThenIfMilledEffect(2, new QueueReflexiveAbilityEffect(new"
         " ExileTargetCardFromGraveyardAndMayCastCopyEffect(AllOf(filter, new CardMaxManaValueXPredicate()), …), false,"
         " true))` — the reflexive graveyard target's X is the triggering spell's mana value (the entry's event value);"),
    ],
}


def apply(path, edits):
    file = ROOT / path
    src = file.read_text()
    out = src
    for old, new in edits:
        if old in out:
            out = out.replace(old, new)
        elif new and new in out:
            continue
        elif not new:
            continue
        else:
            raise SystemExit(f"{path}: neither the old nor the new form of {old[:120]!r}")
    if out != src:
        file.write_text(out)
        print(f"updated {path}")


def main():
    for skill in (".claude/skills/implement-card/SKILL.md", ".claude/skills/review-card/SKILL.md",
                  "agent-docs/EFFECTS_QUICK_REFERENCE.md"):
        file = ROOT / skill
        src = file.read_text()
        out = src
        for old, new in SKILL_EDITS:
            out = out.replace(old, new)
        if out != src:
            file.write_text(out)
            print(f"updated {skill}")
    for path, edits in DOC_EDITS.items():
        apply(path, edits)
    left = [str(p) for p in [ROOT / ".claude/skills/implement-card/SKILL.md", ROOT / ".claude/skills/review-card/SKILL.md"]
            + list((ROOT / "agent-docs").glob("*.md"))
            if re.search(r"OncePerTurnTriggerEffect|AmassGoblinsEqualToCastSpellManaValueEffect|"
                         r"PutCountersOnSelfEqualToTriggeringSpellManaValueEffect|"
                         r"ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect",
                         p.read_text())]
    if left:
        raise SystemExit(f"removed classes still named in: {left}")


if __name__ == "__main__":
    main()
