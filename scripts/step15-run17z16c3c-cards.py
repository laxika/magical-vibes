#!/usr/bin/env python3
"""Step 15, run 17z16c3c: the cards whose follow-up was written with a permanent's id bound in read the entry.

A library-to-battlefield effect's follow-up (`LibrarySelectionFollowUp`) no longer builds its effect from the ids of
the permanents selected (`createEffect(List<UUID>)`): it is the card's own effect (`effect()`), and the choice handler
records the permanents put onto the battlefield as the picks of the entry the follow-up resolves in
(`StackEntry.recordPicks`; one permanent is also the entry's chosen permanent). `QueueReflexiveAbilityEffect` has no
permanent id: `QueueReflexiveAbilityEffect.aboutPickedPermanent(effect)` is the reflexive ability about the next
picked permanent, which is its source permanent.

Oracle text checked with the Scryfall MCP:

- Vivien's Invocation (M19 209): "Look at the top seven cards of your library. You may put a creature card from
  among them onto the battlefield. Put the rest on the bottom of your library in a random order. When a creature is
  put onto the battlefield this way, it deals damage equal to its power to target creature an opponent controls." A
  reflexive ability (CR 603.12) about the creature put onto the battlefield: `aboutPickedPermanent`.
- Masterpiece Vault (YDFT 29): "... draft a card from Masterpiece Vault's spellbook and put it onto the battlefield.
  When an Equipment enters this way, attach it to up to one target creature you control." A reflexive ability about
  the Equipment; `AttachSelectedEquipmentToTargetCreatureEffect(UUID)` is gone for the existing
  `AttachCreatedEquipmentToTargetCreatureEffect`, which attaches the reflexive ability's source permanent.
- Adaptive Armorer (YECL 1): "When this creature enters, draft a card from Adaptive Armorer's spellbook and put it
  onto the battlefield. When an Equipment enters this way, attach it to target creature you control." The same.
- Winota, Joiner of Forces (IKO 216): "... You may put a Human creature card from among them onto the battlefield
  tapped and attacking. It gains indestructible until end of turn. ..." The same resolution:
  `MakeChosenPermanentAttackingEffect.pickedPermanent()` and `GrantKeywordToChosenCreatureUntilEndOfTurnEffect` on the
  entry's chosen permanent.
- Jet, Rebel Leader (TLE 78): "... You may put a creature card with mana value 3 or less from among them onto the
  battlefield tapped and attacking. ..." The same resolution.
- Arthur, Marigold Knight (BLC 41): "... You may put a creature card from among them onto the battlefield tapped and
  attacking. Put the rest on the bottom of your library in a random order. Return that creature to its owner's hand
  at end of combat." The same resolution; `ReturnSelectedPermanentToHandAtEndOfCombatEffect()` reads the pick.
- Three Tree Battalion (YBLB 4): "... You may put a creature card with mana value 3 or less from among them onto the
  battlefield, then conjure a duplicate of that card onto the battlefield. The duplicate perpetually has base power
  and toughness 1/1. ..." The same resolution; `ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(1, 1)` reads
  the pick.

Not converted, because their classes write nothing that changed: Curse of the Werefox (WOE 167: "... When you do,
that creature fights up to one target creature you don't control."; its handler passes
`QueueReflexiveAbilityEffect.aboutEntryTarget`), Dain Ironfoot, Ratonhnhaké:ton and Forum Filibuster
(`CreateTokenThenEffect.aboutThatToken`; their handler records the tokens as the entry's picks), Earth Rumble (TLA
174), Nihiloor (AFC 53), Kaboom! (ONS 214), Strax, Sontaran Nurse (WHO 160), Hidetsugu, Devouring Chaos (NEO 99) and
Lazav, Familiar Stranger (OTJ 216) (their handlers changed), Palico, Armored Skyhunter and Gilgamesh (their follow-ups
are effects that are their own `LibrarySelectionFollowUp`).

Card tests are added (`TEST_EDITS`), each resolving the case through the engine:

- Strax, Sontaran Nurse: the reflexive ability's targets are the other creatures of one player, the player chosen at
  random, whom its entry records; a target that player no longer controls as the ability resolves is not fought.
- Nihiloor: the reflexive ability's entry records that player and the tapped creature, and its targets are that
  player's creatures within the tapped creature's power.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c3c-cards.py [LETTER...]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

CREATE_EFFECT = "        public CardEffect createEffect(List<UUID> selectedPermanentIds) {"
EFFECT = "        public CardEffect effect() {"
CREATE_EFFECT_DEEP = "                public CardEffect createEffect(List<UUID> selectedPermanentIds) {"
EFFECT_DEEP = "                public CardEffect effect() {"

EDITS = {
    "ViviensInvocation": [
        (CREATE_EFFECT, EFFECT),
        ("""            return new QueueReflexiveAbilityEffect(
                    new SourcePermanentDealsPowerDamageToTargetCreatureEffect(new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))),
                    selectedPermanentIds.getFirst());""",
         """            return QueueReflexiveAbilityEffect.aboutPickedPermanent(
                    new SourcePermanentDealsPowerDamageToTargetCreatureEffect(new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));"""),
    ],
    "MasterpieceVault": [
        (CREATE_EFFECT, EFFECT),
        ("""            return new QueueReflexiveAbilityEffect(
                    new AttachSelectedEquipmentToTargetCreatureEffect(selectedPermanentIds.getFirst()), true);""",
         """            return QueueReflexiveAbilityEffect.aboutPickedPermanent(
                    new AttachCreatedEquipmentToTargetCreatureEffect(), true);"""),
        ("import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToTargetCreatureEffect;",
         "import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;"),
    ],
    "AdaptiveArmorer": [
        (CREATE_EFFECT, EFFECT),
        ("""            return new QueueReflexiveAbilityEffect(
                    new AttachSelectedEquipmentToTargetCreatureEffect(selectedPermanentIds.getFirst()));""",
         """            return QueueReflexiveAbilityEffect.aboutPickedPermanent(
                    new AttachCreatedEquipmentToTargetCreatureEffect());"""),
        ("import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToTargetCreatureEffect;",
         "import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;"),
    ],
    "WinotaJoinerOfForces": [
        (CREATE_EFFECT, EFFECT),
        ("""            UUID selectedPermanentId = selectedPermanentIds.getFirst();
            return SequenceEffect.of(
                    new MakeChosenPermanentAttackingEffect(selectedPermanentId),
                    new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                            Keyword.INDESTRUCTIBLE, selectedPermanentId));""",
         """            return SequenceEffect.of(
                    MakeChosenPermanentAttackingEffect.pickedPermanent(),
                    new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                            Keyword.INDESTRUCTIBLE, null, true));"""),
    ],
    "JetRebelLeader": [
        (CREATE_EFFECT, EFFECT),
        ("            return new MakeChosenPermanentAttackingEffect(selectedPermanentIds.getFirst());",
         "            return MakeChosenPermanentAttackingEffect.pickedPermanent();"),
    ],
    "ArthurMarigoldKnight": [
        (CREATE_EFFECT_DEEP, EFFECT_DEEP),
        ("""                    UUID selectedPermanentId = selectedPermanentIds.getFirst();
                    return SequenceEffect.of(
                            new MakeChosenPermanentAttackingEffect(selectedPermanentId),
                            ReturnSelectedPermanentToHandAtEndOfCombatEffect
                                    .forSelectedPermanent(selectedPermanentIds));""",
         """                    return SequenceEffect.of(
                            MakeChosenPermanentAttackingEffect.pickedPermanent(),
                            new ReturnSelectedPermanentToHandAtEndOfCombatEffect());"""),
    ],
    "ThreeTreeBattalion": [
        (CREATE_EFFECT, EFFECT),
        ("""            return new ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(
                    selectedPermanentIds.getFirst(), 1, 1);""",
         """            return new ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(1, 1);"""),
    ],
}

TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

TEST_EDITS = {
    "StraxSontaranNurseTest": [
        ("""    @Test
    void activationWithNoOtherCreaturesStillPaysCostsButDoesNotFight() {""",
         """    @Test
    void reflexiveAbilityTargetsOnlyAnotherCreatureOfThePlayerChosenAtRandom() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        java.util.UUID thatPlayer = gqs.findPermanentController(gd, choice.validPermanentIds().getFirst());
        List<java.util.UUID> othersOfThatPlayer = gd.playerBattlefields.get(thatPlayer).stream()
                .filter(permanent -> gqs.isCreature(gd, permanent) && permanent != strax)
                .map(Permanent::getId)
                .toList();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrderElementsOf(othersOfThatPlayer);

        harness.handlePermanentChosen(player1, choice.validPermanentIds().getFirst());

        assertThat(gd.stack.getLast().getTriggeringPlayerId()).isEqualTo(thatPlayer);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(choice.validPermanentIds().getFirst());
    }

    @Test
    void doesNotFightATargetThatPlayerNoLongerControls() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        java.util.UUID targetId = choice.validPermanentIds().getFirst();
        java.util.UUID thatPlayer = gqs.findPermanentController(gd, targetId);
        Permanent target = gqs.findPermanentById(gd, targetId);
        harness.handlePermanentChosen(player1, targetId);

        java.util.UUID otherPlayer = thatPlayer.equals(player1.getId()) ? player2.getId() : player1.getId();
        gd.playerBattlefields.get(thatPlayer).remove(target);
        gd.playerBattlefields.get(otherPlayer).add(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(otherPlayer)).contains(target);
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activationWithNoOtherCreaturesStillPaysCostsButDoesNotFight() {"""),
    ],
    "NihiloorTest": [
        ("""    @Test
    void targetBecomesIllegalWhenTappedCreaturesPowerDrops() {""",
         """    @Test
    void reflexiveAbilityRecordsThatPlayerAndTheTappedCreature() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent withinPower = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        Permanent tooBig = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        tooBig.setPowerModifier(3);
        Permanent ownCreature = addCreatureReady(player1, new BalefulStrix());
        castNihiloor();

        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(withinPower.getId())
                .doesNotContain(tooBig.getId(), ownCreature.getId(), tapper.getId());
        harness.handlePermanentChosen(player1, withinPower.getId());

        assertThat(gd.stack.getLast().getTriggeringPlayerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getLast().getTriggeringPermanentId()).isEqualTo(tapper.getId());
    }

    @Test
    void targetBecomesIllegalWhenTappedCreaturesPowerDrops() {"""),
    ],
}

IMPORT = re.compile(r"^import ([\w.]+)\.(\w+);\n", re.M)


def without_unused_imports(text):
    """Drops the imports the edits left unused (a type the class no longer names)."""
    body = IMPORT.sub("", text)

    def keep(match):
        return match.group(0) if re.search(r"\b" + match.group(2) + r"\b", body) else ""

    return IMPORT.sub(keep, text)


def main():
    letters = {arg.lower() for arg in sys.argv[1:]}
    changed = 0
    for name, edits in sorted(EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in edits:
            if old in text:
                text = text.replace(old, new, 1)
            elif new not in text:
                sys.exit(f"{name}: edit does not apply:\n{old}")
        if text != original:
            text = without_unused_imports(text)
            path.write_text(text)
            changed += 1
            print(f"converted {path.relative_to(ROOT)}")
    for name, edits in sorted(TEST_EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / TESTS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in edits:
            if new in text:
                continue
            if old in text:
                text = text.replace(old, new, 1)
            else:
                sys.exit(f"{name}: edit does not apply:\n{old}")
        if text != original:
            path.write_text(text)
            changed += 1
            print(f"converted {path.relative_to(ROOT)}")
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
