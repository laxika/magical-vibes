#!/usr/bin/env python3
"""Step 15 run 17d of effect-slot-refactor.md: agent-docs edits.

Idempotent. Each edit names a line by a prefix that must match exactly one line of its file (a line already replaced
is found by its new text and left alone); it is replaced, deleted (``None``) or followed by new lines. The script fails
if a removed form is still named in agent-docs/ or the skills.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

TP = "`PermanentControlledByTriggeringPlayerPredicate`"

DESTROY_LINE = (
    '- "Destroy target [permanent] that player controls" on a damage-to-a-player trigger '
    "(`Triggers.dealsDamageToPlayer()`, `dealsDamageToOpponent()`, `dealsCombatDamageToPlayer()`, the attached, ally "
    "and temporary forms): `DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(<filter>, new "
    "PermanentControlledByTriggeringPlayerPredicate())))`. The collector chooses the target as the trigger goes on the "
    "stack with the player dealt damage as the choice's triggering player, and the stack entry's triggering player "
    'rechecks it as it resolves. "N or more damage" is the trigger\'s event condition, '
    "`TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(N))` (Deus of Calamity). The same predicate "
    "restricts `ExileTargetPermanentEffect` (Lightwielder Paladin, O-Kagachi), `ReturnToHandEffect.target` (Sigil of "
    "Sleep, Arm with Aether), `GainControlOfTargetEffect.withTargetPredicate` (Wake the Dragon), "
    "`TransformSelfAndAttachToTargetCreatureEffect` (Soul Seizer), `SacrificeSelfThenEffect(new "
    "DestroyTargetPermanentEffect(...))` (Blind Zealot) and `TapPermanentsEffect(TapUntapScope.TARGET, ...)` with "
    "`DoesntUntapEffect.targetWhileSourceOnBattlefield()` or `SkipNextUntapEffect(TapUntapScope.TARGET)` (Somnophore, "
    'Mana Skimmer). "Up to one target" is a card target group of zero to one with the predicate in its filter (Feline '
    'Sovereign). "Of that player\'s choice" is the card\'s `setCombatTriggerTargetChosenByDefendingPlayer(true)` '
    "(Bladegriff Prototype, Erithizon)")
SACRIFICE_QR = (
    '- `SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate)` — "choose target [permanent] that player '
    'controls. The player sacrifices that permanent" on a damage-to-a-player trigger: its target is restricted by '
    f"{TP} (chosen as the trigger goes on the stack, rechecked as it resolves) and the target's controller sacrifices "
    "it, indestructible or not. Ashling, the Extinguisher")
DAMAGE_QR = (
    '- "Deals [N / that much] damage to target creature that player controls": `DealDamageToTargetCreatureEffect(amount, '
    f"false, new PermanentControlledByTriggeringPlayerPredicate())` (`(int, predicate)` for a number), `EventValue()` "
    'for "that much", the damage the trigger event dealt: Spark Mage, Mordant Dragon, Commando Raid, The Sea Devils, '
    "Fear of Burning Alive (`Triggers.sourceYouControlDealsNoncombatDamageToOpponent()`), Satyr Firedancer "
    "(`Triggers.instantOrSorceryYouControlDealsDamageToOpponent()`, once for each opponent dealt damage)")
GAMBIT = (
    "`DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount, boolean)` is Flaming Gambit's resolution-time "
    "redirection to a creature the spell's target player (or a targeted planeswalker's controller) controls, chosen by "
    "that player when the boolean is true; it combines with `MayEffect` and `ConditionalReplacementEffect`. A triggered "
    'ability\'s "target creature that player controls" is `DealDamageToTargetCreatureEffect` with '
    f"{TP}.")
CURSE_QR = (
    '- "Whenever a creature attacks enchanted player, put a [counter] on it": '
    "`Triggers.creatureAttacks().withFilter(new PermanentIsAttackingEnchantedPlayerPredicate())` with "
    "`PutCounterOnEachMatchingPermanentEffect(counter, 1, new PermanentIsTriggeringPermanentPredicate(), "
    "EachPermanentScope.ALL_PLAYERS)`; attacks on that player's planeswalkers don't match (Curse of Predation)")

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount, boolean)` uses", "- " + GAMBIT),
        ("- `DestroyPermanentDamagedPlayerControlsEffect(PermanentPredicate, int minimumDamage)`", DESTROY_LINE),
        ("- `ReturnPermanentDamagedPlayerControlsToHandEffect(PermanentPredicate)`", None),
        ("- `DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect(PermanentPredicate)`", None),
        ("- `GainControlOfPermanentDamagedPlayerControlsEffect(PermanentPredicate)`", None),
        ("- `SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate, int minimumDamage)`", SACRIFICE_QR),
        ("- `DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount)`; convenience `(int)`", DAMAGE_QR),
        (" - `DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount)`; `(int)`", None),
        ("- `PutCounterOnAttackingCreatureOnAttacksYouEffect(CounterType)`", CURSE_QR),
        ("- `TapChosenPermanentEffect(PermanentPredicate, boolean preventUntapWhileSourceTapped)`", None),
        ("- `TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect()`", None),
    ],
    "EFFECTS_INDEX.md": [
        ("`DealDamageToTargetCreatureDamagedPlayerControlsEffect(DynamicAmount, boolean)` uses", GAMBIT),
        ("| `DealDamageToTargetCreatureDamagedPlayerControlsEffect` | `(DynamicAmount damage)`; convenience `(int)` |",
         "| `DealDamageToTargetCreatureDamagedPlayerControlsEffect` | `(DynamicAmount damage, boolean "
         "targetPlayerChooses)`; convenience `(int)`, `(int, boolean)` | Flaming Gambit's resolution-time redirection to "
         "a creature the spell's target player controls, chosen by that player when `targetPlayerChooses`. A triggered "
         "ability's \"target creature that player controls\" is `DealDamageToTargetCreatureEffect` with "
         f"{TP} |"),
        ("| `DealDamageToTargetCreatureDamagedPlayerControlsEffect` | `(DynamicAmount damage)`; convenience `(int damage)` |",
         None),
        ("| `SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect` |", None),
        ("| `TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect` |", None),
        ("| `PutImprintedCardIntoOwnersHandEffect` |",
         "| `PutImprintedCardIntoOwnersHandEffect` | `()` | when this permanent dies, put the exiled card into its "
         "owner's hand (Hoarding Dragon); reads the stack entry's linked exiled card (`StackEntry.linkedExiledCardId`, "
         "set by the death collector as the ability triggers, CR 607.2a), so the Dragon returning and exiling another "
         "card can't redirect it |"),
        ("| `ExilePermanentDamagedPlayerControlsEffect` |", None),
        ("| `DestroyPermanentDamagedPlayerControlsEffect` |",
         "| `DestroyPermanentDamagedPlayerControlsEffect` | `(PermanentPredicate predicate)` | Dokuchi Silencer's "
         "reflexive \"when you do, destroy target … that player controls\" inside `DiscardCardThenEffect`: "
         "`DiscardCardThenEffectHandler` binds the damaged player through `forDamagedPlayer` before the reflexive "
         "target is chosen. A triggered ability's own \"destroy target … that player controls\" is "
         f"`DestroyTargetPermanentEffect` with {TP} |"),
        ("| `DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect` |", None),
        ("| `GainControlOfPermanentDamagedPlayerControlsEffect` |", None),
        ("| `SacrificePermanentDamagedPlayerControlsEffect` |",
         "| `SacrificePermanentDamagedPlayerControlsEffect` | `(PermanentPredicate predicate)` | \"choose target "
         "[permanent] that player controls. The player sacrifices that permanent\" on a damage-to-a-player trigger: the "
         f"target is restricted by {TP} (chosen as the trigger goes on the stack, rechecked as it resolves) and its "
         "controller sacrifices it (ignores indestructible/regeneration; fires sacrifice triggers). Ashling, the "
         "Extinguisher |"),
        ("| `TapChosenPermanentEffect` |", None),
        ("| `PutCounterOnAttackingCreatureOnAttacksYouEffect` |", None),
        ("- `DestroyPermanentDamagedPlayerControlsEffect.forDamagedPlayer(UUID)` composes",
         '- "That player controls" of a damage-to-a-player trigger is '
         f"{TP} in the effect's target restriction: the collector chooses the target as the trigger goes on the stack "
         "against the player dealt damage (the choice's triggering player), and the stack entry's triggering player "
         "rechecks it as it resolves."),
        ("- `ReturnPermanentDamagedPlayerControlsToHandEffect(PermanentPredicate)` is a damage-trigger marker", None),
        ("`TapChosenPermanentEffect.forDamagedPlayer(UUID)` binds", None),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Combat damage with last-turn attack condition → exile a damaged player's nonland permanent |",
         "| Combat damage with last-turn attack condition → exile a damaged player's nonland permanent | "
         "`o/OKagachiVengefulKami.java` | `Triggers.dealsCombatDamageToPlayer()` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerAttackedControllerDuringLastTurn())` and "
         "`ExileTargetPermanentEffect(allOf(nonland, new PermanentControlledByTriggeringPlayerPredicate()))`; the "
         "condition checks the damaged player against the source controller's stored last-turn attack history |"),
        ("| Combat damage → may deal damage to a creature the damaged player controls |",
         "| Combat damage → may deal damage to a creature the damaged player controls | `s/SparkMage.java` | "
         "`MayEffect(DealDamageToTargetCreatureEffect(1, new PermanentControlledByTriggeringPlayerPredicate()))` on "
         "`Triggers.dealsCombatDamageToPlayer()` — the creature is targeted as the trigger goes on the stack; the may "
         "is asked as it resolves |"),
        ("| Combat damage may-sacrifice-destroy |",
         "| Combat damage may-sacrifice-destroy | `b/BlindZealot.java` | Triggers.dealsCombatDamageToPlayer() "
         "`MayEffect(SacrificeSelfThenEffect(DestroyTargetPermanentEffect(allOf(creature, new "
         "PermanentControlledByTriggeringPlayerPredicate()))))` — the creature is targeted as the trigger goes on the "
         "stack; sacrificing is decided as it resolves |"),
        ("| Combat damage sacrifice-target-creature |",
         "| Combat damage sacrifice-target-creature | `a/AshlingTheExtinguisher.java` | "
         "Triggers.dealsCombatDamageToPlayer() `SacrificePermanentDamagedPlayerControlsEffect(PermanentIsCreaturePredicate)` "
         "— mandatory; the controller targets a creature that player controls, that player sacrifices it |"),
        ("| Combat damage gain control of damaged player's artifact |",
         "| Combat damage gain control of damaged player's artifact | `w/WakeTheDragon.java` | The created Dragon token "
         "carries Triggers.dealsCombatDamageToPlayer() `GainControlOfTargetEffect.withTargetPredicate(PERMANENT, "
         "allOf(artifact, new PermanentControlledByTriggeringPlayerPredicate()))` |"),
        ("| Combat damage may-transform-attach |",
         "| Combat damage may-transform-attach | `s/SoulSeizer.java` (back face `g/GhastlyHaunting.java`) | "
         "Triggers.dealsCombatDamageToPlayer() `MayEffect(TransformSelfAndAttachToTargetCreatureEffect(allOf(creature, "
         "new PermanentControlledByTriggeringPlayerPredicate())))` — the creature is targeted as the trigger goes on "
         "the stack; if the may is accepted, transform to the Aura back face, attach, and steal via "
         "`ControlEnchantedCreatureEffect` on the back face |"),
        ("| Unblocked may-sacrifice-destroy (can't regen) |",
         "| Unblocked may-sacrifice-destroy (can't regen) | `n/Necrite.java` | "
         "`target(TargetFilters.creatureAnOpponentControls())` + Triggers.attacksUnblocked() "
         "`MayEffect(SequenceEffect.of(new SacrificeSelfEffect(), new DestroyTargetPermanentEffect(true)))` — the "
         "target is chosen as the trigger goes on the stack; sacrificing is decided as it resolves |"),
        ("| Whenever an instant/sorcery you control damages an opponent: damage that opponent's creature for that much |",
         "| Whenever an instant/sorcery you control damages an opponent: damage that opponent's creature for that much | "
         "`s/SatyrFiredancer.java` | `Triggers.instantOrSorceryYouControlDealsDamageToOpponent()` "
         "`DealDamageToTargetCreatureEffect(new EventValue(), false, new "
         "PermanentControlledByTriggeringPlayerPredicate())` — once for each opponent dealt damage, that opponent the "
         "triggering player and the damage dealt to them the event value; the creature is targeted as the trigger goes "
         "on the stack |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ('| "Whenever a creature attacks enchanted player, put a +1/+1 counter on it" |',
         '| "Whenever a creature attacks enchanted player, put a +1/+1 counter on it" | '
         "`PutCounterOnEachMatchingPermanentEffect(PLUS_ONE_PLUS_ONE, 1, new PermanentIsTriggeringPermanentPredicate(), "
         "EachPermanentScope.ALL_PLAYERS)` | `Triggers.creatureAttacks().withFilter(new "
         "PermanentIsAttackingEnchantedPlayerPredicate())` | Attacks against that player's planeswalkers don't match. "
         "Curse of Predation (IMA 159) |"),
        ('| "Whenever CARDNAME deals N or more damage to an opponent, destroy target land that player controls" |',
         '| "Whenever CARDNAME deals N or more damage to an opponent, destroy target land that player controls" | '
         "`DestroyTargetPermanentEffect(allOf(new PermanentIsLandPredicate(), new "
         "PermanentControlledByTriggeringPlayerPredicate()))` | `Triggers.dealsDamageToOpponent()` with "
         "`TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(N))` | Deus of Calamity (N=6). The land is "
         "targeted as the trigger goes on the stack, among the damaged player's |"),
        ('| "Whenever this creature deals combat damage to a player, destroy target nonland permanent of that player\'s',
         '| "Whenever this creature deals combat damage to a player, destroy target nonland permanent of that player\'s '
         'choice that one of your opponents controls" | `DestroyTargetPermanentEffect(allOf(new PermanentNotPredicate(new '
         "PermanentIsLandPredicate()), new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))` "
         "+ `setCombatTriggerTargetChosenByDefendingPlayer(true)` | Triggers.dealsCombatDamageToPlayer() | Bladegriff "
         "Prototype (OTC 253). The damaged player (the creature's defending player) chooses the target as the trigger "
         "goes on the stack |"),
        ('| "Whenever CARDNAME deals combat damage to a player, if that player attacked you during their last turn,',
         '| "Whenever CARDNAME deals combat damage to a player, if that player attacked you during their last turn, '
         'exile target nonland permanent that player controls" | `ExileTargetPermanentEffect(allOf(new '
         "PermanentNotPredicate(new PermanentIsLandPredicate()), new PermanentControlledByTriggeringPlayerPredicate()))` "
         "with `TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerAttackedControllerDuringLastTurn())` | "
         "Triggers.dealsCombatDamageToPlayer() | O-Kagachi, Vengeful Kami (DMC 164; MB2 138). The intervening \"if\" "
         "is checked against the damaged player and source controller |"),
        ('| "Whenever O-Kagachi deals combat damage to a player, if that player attacked you during their last turn,',
         None),
        ('| "Whenever this token deals combat damage to a player, gain control of target artifact that player controls" |',
         '| "Whenever this token deals combat damage to a player, gain control of target artifact that player controls" '
         "| `GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT, allOf(new "
         "PermanentIsArtifactPredicate(), new PermanentControlledByTriggeringPlayerPredicate()))` | token "
         "`Triggers.dealsCombatDamageToPlayer()` | Wake the Dragon (LTC). The artifact is targeted as the trigger goes "
         "on the stack |"),
        ('| "Whenever enchanted creature deals damage to a player, return target creature that player controls to its',
         '| "Whenever enchanted creature deals damage to a player, return target creature that player controls to its '
         "owner's hand\" | `target(TargetFilters.creature()).addTrigger(Triggers.dealsDamageToPlayer(), "
         "ReturnToHandEffect.target(allOf(new PermanentIsCreaturePredicate(), new "
         "PermanentControlledByTriggeringPlayerPredicate())))` | Triggers.dealsDamageToPlayer() | Sigil of Sleep. The "
         "creature is targeted as the trigger goes on the stack; combat and noncombat damage |"),
        ('| "Whenever CARDNAME deals combat damage to a player, choose target creature that player controls.',
         '| "Whenever CARDNAME deals combat damage to a player, choose target creature that player controls. The player '
         'sacrifices that creature." | `SacrificePermanentDamagedPlayerControlsEffect(new PermanentIsCreaturePredicate())` '
         "| Triggers.dealsCombatDamageToPlayer() | Ashling, the Extinguisher. Mandatory; the controller targets a "
         "creature that player controls, that player sacrifices it (ignores indestructible) |"),
        ('| "Whenever CARDNAME deals combat damage to a player, you may have it deal N damage to target creature that',
         '| "Whenever CARDNAME deals combat damage to a player, you may have it deal N damage to target creature that '
         'player controls" | `MayEffect(DealDamageToTargetCreatureEffect(N, new '
         'PermanentControlledByTriggeringPlayerPredicate()), "...")`; `(new EventValue(), false, ...)` for "that much" '
         "| Triggers.dealsCombatDamageToPlayer() | Spark Mage / Mordant Dragon. The creature is targeted as the trigger "
         "goes on the stack; the may is asked as it resolves |"),
        ('| "Whenever an instant or sorcery spell you control deals damage to an opponent, this creature deals that',
         '| "Whenever an instant or sorcery spell you control deals damage to an opponent, this creature deals that much '
         'damage to target creature that player controls" | `DealDamageToTargetCreatureEffect(new EventValue(), false, '
         "new PermanentControlledByTriggeringPlayerPredicate())` | "
         "`Triggers.instantOrSorceryYouControlDealsDamageToOpponent()` | Satyr Firedancer. Triggers once for each "
         "opponent dealt damage (CR 603.2c), that opponent its triggering player and the damage dealt to them its event "
         "value; the creature is targeted as the trigger goes on the stack |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.instantOrSorceryYouControlDealsDamage()` |",
         "| `Triggers.instantOrSorceryYouControlDealsDamage()` | \"Whenever an instant or sorcery spell you control "
         "deals damage, ...\" — fires once per damage event no matter how many objects the spell damaged "
         "simultaneously, only for spells their controller controls. Shares the batched non-combat choke point behind "
         "`Triggers.sourceDealsDamage()`; the summed total is snapshotted onto the trigger's `eventValue`. \"Deals "
         "damage to an opponent\" is `Triggers.instantOrSorceryYouControlDealsDamageToOpponent()` (below). Blaze "
         "Commando |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| combat damage → damaged player chooses a nonland permanent controlled by one of the source controller's",
         "| combat damage → damaged player chooses a nonland permanent controlled by one of the source controller's "
         "opponents to destroy | `b/BladegriffPrototype.java` + `setCombatTriggerTargetChosenByDefendingPlayer(true)` |"),
    ],
}

# New lines after an anchor line (by prefix), each skipped when already present.
INSERTS = {
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentControlledByTriggeringPermanentControllerPredicate` |",
         "| `PermanentControlledByTriggeringPlayerPredicate` | `()` | permanents controlled by the player the triggered "
         "ability's event was about (`FilterContext.triggeringPlayerId`: the choice's triggering player as the target is "
         "chosen, the stack entry's as it resolves); \"target creature that player controls\" of a damage-to-a-player "
         "trigger (Trygon Predator, Mordant Dragon, Satyr Firedancer); nothing without one | `gameData` + "
         "`triggeringPlayerId` |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.instantOrSorceryYouControlDealsDamage()` |",
         "| `Triggers.instantOrSorceryYouControlDealsDamageToOpponent()` | \"Whenever an instant or sorcery spell you "
         "control deals damage to an opponent, ...\" — once for each opponent the spell dealt damage to (CR 603.2c), "
         "that opponent the stack entry's triggering player (\"that player\") and the damage dealt to them its "
         "`eventValue`; a targeted effect's target is chosen as the trigger goes on the stack. Satyr Firedancer |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| `Triggers.creatureYouControlDealsCombatDamageToPlayer()` (targeting variants) |",
         "| \"Target [permanent] that player controls\" on any damage-to-a-player trigger | Every collector that queues "
         "an `AttackTriggerTarget` for a damage-to-a-player event gives it the player dealt damage "
         "(`withTriggeringPlayer`), which `PermanentControlledByTriggeringPlayerPredicate` reads through "
         "`FilterContext.triggeringPlayerId` as the target is chosen; the stack entry keeps it "
         "(`StackEntry.triggeringPlayerId`) for the resolution recheck. No collector binds the player into an effect | "
         "Damage |"),
    ],
}

REMOVED = [
    "TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect", "SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect",
    "ReturnPermanentDamagedPlayerControlsToHandEffect", "GainControlOfPermanentDamagedPlayerControlsEffect",
    "ExilePermanentDamagedPlayerControlsEffect", "DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect",
    "TapChosenPermanentEffect", "PutCounterOnAttackingCreatureOnAttacksYouEffect",
    "setAttackTriggerTargetChosenByDefendingPlayer", "boundCardId",
]


def edit(name, edits, inserts):
    path = DOCS / name
    lines = path.read_text().split("\n")
    changed = False
    for prefix, new in edits:
        matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if new is not None and new in lines:
            continue
        if new is None and not matches:
            continue
        if len(matches) != 1:
            sys.exit(f"{path}: expected one line starting {prefix!r}, found {len(matches)}")
        if new is None:
            del lines[matches[0]]
        else:
            lines[matches[0]] = new
        changed = True
    for prefix, new in inserts:
        if new in lines:
            continue
        matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if len(matches) != 1:
            sys.exit(f"{path}: expected one anchor line starting {prefix!r}, found {len(matches)}")
        lines.insert(matches[0] + 1, new)
        changed = True
    if changed:
        path.write_text("\n".join(lines))
    print(("edited " if changed else "unchanged ") + str(path.relative_to(ROOT)))


def main():
    for name in sorted(set(EDITS) | set(INSERTS)):
        edit(name, EDITS.get(name, []), INSERTS.get(name, []))
    stale = []
    for path in list(DOCS.rglob("*.md")) + list((ROOT / ".claude" / "skills").rglob("*.md")):
        text = path.read_text()
        stale += [f"{path.relative_to(ROOT)}: {name}" for name in REMOVED if re.search(r"\b" + name + r"\b", text)]
    if stale:
        sys.exit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
