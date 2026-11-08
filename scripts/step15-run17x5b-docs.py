#!/usr/bin/env python3
"""Step 15, run 17x5b: the agent-docs entries of `CopyControllerActivatedAbilityTriggerEffect`, which is gone.

"Copy that ability" of an activation trigger is the existing `CopyTriggeringSpellOrAbilityEffect`, wrapped as the card
writes it, with the record's conditions as `AbilityTrigger` filters (`withKind` LOYALTY / EXHAUST / WITH_X_IN_COST,
`withSource`, `withStackEntryFilter`, `withPayment` LIFE / SACRIFICE) and "an ability of equipped creature is
activated" the new `Triggers.playerActivatesNonManaAbility()`. Each edit replaces a whole line found by a unique
prefix (or, for a line kept, a fragment); a line already rewritten is left alone. Fails if a doc or skill still names
the record. Idempotent.

Usage: step15-run17x5b-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

COPY_ENTRY_QR = (
    "- `CopyTriggeringSpellOrAbilityEffect()` — \"copy that spell or ability\" (Bill Potts) and \"copy that ability\" of an "
    "activation trigger: copies the stack entry's triggering spell, or the activated ability an activation trigger was "
    "about, for this ability's controller (CR 707.10; an Equipment's controller controls the copy of an opponent's "
    "creature's ability); the copy's controller may choose new targets. Wrap it as the card writes it: \"you may pay {2}. "
    "If you do, copy that ability\" `MayPayManaEffect(\"{2}\", new CopyTriggeringSpellOrAbilityEffect(), prompt)` (Rings of "
    "Brighthearth, Kurkesh, Battlemage's Bracers, Chandra's Regulator), \"you may copy that ability\" `MayEffect` (Ashnod "
    "the Uncaring), \"you may pay that much life again\" `MayPayLifeEqualToAmountEffect(new EventValue(), …)` (Verrak, the "
    "life paid is the entry's event value); the \"you may\" is asked as the ability resolves (CR 603.5). The activation "
    "trigger's conditions are its filters, judged once the ability is on the stack: `Triggers.youActivateNonManaAbility()` "
    "/ `youActivateAbility()` (mana abilities included) with `.withKind(AbilityTrigger.AbilityKind.LOYALTY)` (Rowan's "
    "Talent, Chandra's Regulator), `EXHAUST` (Sala, Deck Boss), `WITH_X_IN_COST` (Unbound Flourishing), "
    "`.withSource(permanentPredicate)` (\"an ability of an artifact\" `PermanentIsArtifactPredicate`, Kurkesh; CR 109.2, the "
    "permanent), `.withStackEntryFilter(stackEntryPredicate)` (\"that targets a creature or player\", Ertha Jo; Bill Potts; "
    "Leori), `.withPayment(AbilityTrigger.ActivationPayment.LIFE)` (\"if life was paid to activate it\", Verrak) / "
    "`SACRIFICE` (\"if one or more permanents were sacrificed to activate it\", Ashnod); \"whenever an ability of equipped "
    "creature is activated\" is `Triggers.playerActivatesNonManaAbility().withSource(new "
    "PermanentIsHostOfSourceAuraPredicate())` (whoever activates it; Illusionist's Bracers, Battlemage's Bracers)"
)

GONE_QR = (
    "- `CopyControllerActivatedAbilityTriggerEffect` — **Gone** (effect-slot-refactor.md step 15, run 17x5b): \"whenever "
    "you activate an ability …, copy that ability\" is an activation trigger with filters and "
    "`CopyTriggeringSpellOrAbilityEffect()`, wrapped as the card writes it (see its entry)"
)

COPY_ENTRY_INDEX = (
    "| `CopyTriggeringSpellOrAbilityEffect` | `()` | \"copy that spell or ability. You may choose new targets for the copy\" "
    "(Bill Potts, one ability of a cast and an activation condition with `TriggerLimit.ONCE_EACH_TURN`) and \"copy that "
    "ability\" of the activation triggers (Rings of Brighthearth, Kurkesh, Illusionist's and Battlemage's Bracers, Chandra's "
    "Regulator, Rowan's Talent, Sala, Ertha Jo, Leori, Unbound Flourishing, Locus of Enlightenment, Verrak, Ashnod): copies "
    "the stack entry's triggering spell, or the activated ability an activation trigger was about "
    "(`StackEntry.getTriggeringActivatedAbility()`, through `CopyControllerActivatedAbilityEffectHandler`) for this ability's "
    "controller. Written alone or behind a `MayEffect`, `MayPayManaEffect`, `MayPayLifeEqualToAmountEffect` (the life paid "
    "to activate it is the entry's event value) or `ConditionalEffect`; the activation collectors find it there and judge "
    "the trigger's `withStackEntryFilter` and `withPayment` once the ability is on the stack |"
)

GONE_INDEX = (
    "| `CopyControllerActivatedAbilityTriggerEffect` | (gone) | **Gone** (effect-slot-refactor.md step 15, run 17x5b): an "
    "activation trigger with filters (`AbilityTrigger.withKind` LOYALTY / EXHAUST / WITH_X_IN_COST, `withSource`, "
    "`withStackEntryFilter`, `withPayment`; \"an ability of equipped creature\" `Triggers.playerActivatesNonManaAbility()`) "
    "and `CopyTriggeringSpellOrAbilityEffect()`, wrapped as the card writes it |"
)

KIND_QR_OLD = (
    "  `.withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)` / `WITHOUT_TAP_COST` (Imprison, Artifact Possession)\n"
)
KIND_QR_NEW = (
    "  `.withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)` / `WITHOUT_TAP_COST` (Imprison, Artifact Possession),\n"
    "  “an exhaust ability” `EXHAUST` (Sala, Deck Boss), “if that ability's activation cost contains {X}”\n"
    "  `WITH_X_IN_COST` (Unbound Flourishing), “if life was paid / one or more permanents were sacrificed to activate\n"
    "  it” `.withPayment(AbilityTrigger.ActivationPayment.LIFE)` / `SACRIFICE` (Verrak, Ashnod; judged by the\n"
    "  collector of the abilities that copy the activated ability only), “whenever an ability of equipped creature is\n"
    "  activated” (whoever activates it) `Triggers.playerActivatesNonManaAbility().withSource(new\n"
    "  PermanentIsHostOfSourceAuraPredicate())` (Illusionist's Bracers)\n"
)

# (file, line prefix, replacement line or None to delete the line)
LINES = [
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyTriggeringSpellOrAbilityEffect()` — \"copy that spell or ability\" (Bill Potts)", COPY_ENTRY_QR),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect(String manaCost)` — ", GONE_QR),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect(..., exhaustAbilityOnly=true)`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect(null, null, true)`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect(..., sourcePermanentFilter)`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect.withXInActivationCost()`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerActivatedAbilityTriggerEffect(null, sourceFilter)` in", None),
    ("EFFECTS_INDEX.md", "| `CopyTriggeringSpellOrAbilityEffect` | `()` |", COPY_ENTRY_INDEX),
    ("EFFECTS_INDEX.md", "| `CopyControllerActivatedAbilityTriggerEffect` | `(String manaCost)` / `(String manaCost, StackEntryPredicate sourceFilter)` / `(String manaCost, StackEntryPredicate sourceFilter, boolean equippedCreatureOnly)` / full form with `activationCostContainsX` / full form with `exhaustAbilityOnly` |", GONE_INDEX),
    ("EFFECTS_INDEX.md", "| `CopyControllerActivatedAbilityTriggerEffect` | `(String manaCost)` / `(String manaCost, StackEntryPredicate sourceFilter)` / `(String manaCost, StackEntryPredicate sourceFilter, boolean equippedCreatureOnly)` / `withXInActivationCost()` |", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerActivatedAbilityTriggerEffect` | `(String manaCost)` / `(String manaCost, StackEntryPredicate sourceFilter)` / `(String manaCost, StackEntryPredicate sourceFilter, boolean equippedCreatureOnly)` / full form with `activationCostContainsX` |", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerActivatedAbilityEffect` | `(StackEntry abilitySnapshot, ActivatedAbility ability, UUID activatingPlayerId)` |",
     "| `CopyControllerActivatedAbilityEffect` | `(StackEntry abilitySnapshot, ActivatedAbility ability, UUID activatingPlayerId)` | "
     "engine-built resolution effect (never authored by a card): copies the snapshotted activated ability onto the stack "
     "(CR 707.10) for its controller. For a single-target ability, queues a `CopyActivatedAbilityRetargetEffect` "
     "may-ability so the controller may choose a new target for the copy. Built by the delayed and mana-linked ability "
     "copies and `CopyControllerAllSpellsAndAbilitiesEffect`; a card's \"copy that ability\" is "
     "`CopyTriggeringSpellOrAbilityEffect`, whose handler resolves through it |"),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", "| Pay-to-copy your activated abilities | `r/RingsOfBrighthearth.java` |",
     "| Pay-to-copy your activated abilities | `r/RingsOfBrighthearth.java` | Triggers.youActivateNonManaAbility() "
     "`MayPayManaEffect(\"{2}\", new CopyTriggeringSpellOrAbilityEffect(), prompt)` — \"whenever you activate a non-mana "
     "ability, you may pay {2} to copy it (may choose new targets).\" Collected by "
     "`TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers` after the ability is on the stack, the "
     "activated ability its triggering object; the payment is asked as it resolves; single-target copies are retargetable |"),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", "| Copy equipped creature's activated abilities | `i/IllusionistsBracers.java` |",
     "| Copy equipped creature's activated abilities | `i/IllusionistsBracers.java` | "
     "Triggers.playerActivatesNonManaAbility().withSource(new PermanentIsHostOfSourceAuraPredicate()) "
     "`CopyTriggeringSpellOrAbilityEffect()` + EquipActivatedAbility(\"{3}\") — free, mandatory copy of any non-mana "
     "ability of the creature this Equipment is attached to, whoever activates it; copy is retargetable and controlled "
     "by the Equipment's controller |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you activate an ability, if it isn't a mana ability, you may pay {2}. If you do, copy that ability\" |",
     "| \"whenever you activate an ability, if it isn't a mana ability, you may pay {2}. If you do, copy that ability\" | "
     "`MayPayManaEffect(\"{2}\", new CopyTriggeringSpellOrAbilityEffect(), \"Pay {2} to copy that ability?\")` | "
     "Triggers.youActivateNonManaAbility() | Rings of Brighthearth — collected once the non-mana ability is on the "
     "stack (its triggering object); the payment is asked as it resolves (CR 603.5); single-target copies may be "
     "retargeted. \"Whenever you activate a loyalty ability of a Chandra planeswalker, you may pay {1}\": "
     "`.withKind(AbilityTrigger.AbilityKind.LOYALTY).withStackEntryFilter(…)` (Chandra's Regulator); \"of enchanted "
     "planeswalker, copy that ability\": `.withKind(LOYALTY).withSource(AllOf(PermanentIsPlaneswalkerPredicate, "
     "PermanentIsHostOfSourceAuraPredicate))` (Rowan's Talent); \"an exhaust ability that isn't a mana ability, copy it\": "
     "`.withKind(AbilityTrigger.AbilityKind.EXHAUST)` (Sala, Deck Boss); \"an ability that targets a creature or "
     "player\": `.withStackEntryFilter(…)` (Ertha Jo); \"if life was paid to activate it, you may pay that much life "
     "again\": `.withPayment(AbilityTrigger.ActivationPayment.LIFE)` with `MayPayLifeEqualToAmountEffect(new EventValue(), "
     "…)` (Verrak); \"of an artifact or creature …, if one or more permanents were sacrificed to activate it, you may "
     "copy\": `.withSource(…).withPayment(SACRIFICE)` with `MayEffect` (Ashnod the Uncaring) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a permanent spell with a mana cost that contains {X}, double the value of X. Whenever you cast an instant or sorcery spell or activate an ability, if that spell's mana cost or that ability's activation cost contains {X}, copy that spell or ability\" |",
     "| \"Whenever you cast a permanent spell with a mana cost that contains {X}, double the value of X. Whenever you cast "
     "an instant or sorcery spell or activate an ability, if that spell's mana cost or that ability's activation cost "
     "contains {X}, copy that spell or ability\" | `DoubleXValueOfTriggeringSpellEffect()` on a filtered cast trigger + "
     "`Triggers.youCastSpell().withFilter(CardAllOfPredicate(CardHasXInManaCostPredicate, INSTANT_OR_SORCERY))`, `new "
     "CopyTriggeringSpellEffect()` + `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST)`, "
     "`new CopyTriggeringSpellOrAbilityEffect()` | Triggers.youCastSpell() + Triggers.youActivateAbility() | Unbound "
     "Flourishing — spell copies are mandatory, and activated-ability copies include mana abilities while remaining "
     "limited to activations whose mana cost contains `{X}` |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you activate an ability of an artifact, if it isn't a mana ability, you may pay {R}. If you do, copy that ability\" |",
     "| \"whenever you activate an ability of an artifact, if it isn't a mana ability, you may pay {R}. If you do, copy "
     "that ability\" | `MayPayManaEffect(\"{R}\", new CopyTriggeringSpellOrAbilityEffect(), \"Pay {R} to copy that "
     "ability?\")` | Triggers.youActivateNonManaAbility().withSource(new PermanentIsArtifactPredicate()) | Kurkesh, Onakke "
     "Ancient — same as Rings of Brighthearth; \"an artifact\" is the permanent whose ability it is (CR 109.2), so an "
     "artifact card's cycling from the hand doesn't trigger it |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever an ability of equipped creature is activated, if it isn't a mana ability, copy that ability\" |",
     "| \"Whenever an ability of equipped creature is activated, if it isn't a mana ability, copy that ability\" | "
     "`CopyTriggeringSpellOrAbilityEffect()` (\"you may pay {1}. If you do\": `MayPayManaEffect`, Battlemage's Bracers) | "
     "Triggers.playerActivatesNonManaAbility().withSource(new PermanentIsHostOfSourceAuraPredicate()) | Illusionist's "
     "Bracers — whoever activates the ability; the Bracers' controller controls the copy (and pays) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever Leori deals combat damage to a player, choose a planeswalker type.",
     "| \"Whenever Leori deals combat damage to a player, choose a planeswalker type. Until end of turn, whenever you "
     "activate an ability of a planeswalker of that type, copy that ability\" | `Triggers.dealsCombatDamageToPlayer() "
     "ChooseSubtypeForSourceEffect(CardSubtype.planeswalkerTypes(), true, \"Choose a planeswalker type.\")` + "
     "`Triggers.youActivateAbility().withStackEntryFilter(StackEntryAllOfPredicate(PLANESWALKER, "
     "StackEntryHasSourceChosenSubtypePredicate))` `CopyTriggeringSpellOrAbilityEffect()` | triggered ability + "
     "activated-ability trigger | Leori, Sparktouched Hunter (CMM 709). The source subtype choice is temporary and the "
     "ability-activation slot includes mana abilities. |"),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md", "| Cast-time X doubling + X spell/ability copying | `u/UnboundFlourishing.java` |",
     "| Cast-time X doubling + X spell/ability copying | `u/UnboundFlourishing.java` | `Triggers.youCastSpell().withFilter("
     "permanent X spell)` `DoubleXValueOfTriggeringSpellEffect` + `Triggers.youCastSpell().withFilter(CardAllOfPredicate("
     "CardHasXInManaCostPredicate, CardAnyOfPredicate(INSTANT, SORCERY)))` `new CopyTriggeringSpellEffect()` + "
     "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST)` `new "
     "CopyTriggeringSpellOrAbilityEffect()` — doubles X for permanent spells and copies your X instant/sorcery spells and "
     "activated abilities, including mana abilities |"),
]

FRAGMENTS = [
    ("EFFECTS_QUICK_REFERENCE.md",
     "When the source also carries a `CopyControllerActivatedAbilityTriggerEffect`, every temporary choice",
     "When the source also has an activation trigger with `CopyTriggeringSpellOrAbilityEffect` (Leori), every "
     "temporary choice"),
    ("EFFECTS_QUICK_REFERENCE.md", KIND_QR_OLD, KIND_QR_NEW),
]


def edit_lines(text, prefix, replacement, name):
    lines = text.split("\n")
    hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
    if replacement is not None and replacement in lines:
        return text
    if not hits:
        if replacement is None:
            return text
        raise SystemExit(f"{name}: no line starts with {prefix!r}")
    if len(hits) > 1:
        raise SystemExit(f"{name}: {len(hits)} lines start with {prefix!r}")
    if replacement is None:
        del lines[hits[0]]
    else:
        lines[hits[0]] = replacement
    return "\n".join(lines)


def main():
    for name, prefix, replacement in LINES:
        path = DOCS / name
        text = path.read_text()
        new = edit_lines(text, prefix, replacement, name)
        if new != text:
            path.write_text(new)
            print(f"edited {name}: {prefix[:60]}")
    for name, old, new in FRAGMENTS:
        path = DOCS / name
        text = path.read_text()
        if new in text:
            continue
        if text.count(old) != 1:
            raise SystemExit(f"{name}: fragment found {text.count(old)} times: {old[:80]!r}")
        path.write_text(text.replace(old, new))
        print(f"edited {name}: {old[:60]}")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").glob("*/SKILL.md")):
        for number, line in enumerate(path.read_text().split("\n"), 1):
            if "CopyControllerActivatedAbilityTriggerEffect" in line and "**Gone**" not in line:
                stale.append(f"{path.relative_to(ROOT)}:{number}")
    if stale:
        raise SystemExit("still names CopyControllerActivatedAbilityTriggerEffect:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
