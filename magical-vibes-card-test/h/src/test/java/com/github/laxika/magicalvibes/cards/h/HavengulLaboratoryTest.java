package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavengulLaboratory.class, HavengulMystery.class, ShivanDragon.class, Forest.class,
        StrionicResonator.class})
class HavengulLaboratoryTest extends BaseCardTest {

    @Test
    void frontFaceProducesColorlessManaImmediately() {
        Permanent laboratory = addLaboratory();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoSacrificedCluesDoNotTriggerTransformation() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 2);
        sacrificeClues(player1, 2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(laboratory.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTransformAtOpponentsEndStep() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(laboratory.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void backFacePaysOneLifeAndProducesBlackManaImmediately() {
        Permanent laboratory = transformWithoutCreature();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unrelatedPermanentLeavingDoesNotTriggerBackFace() {
        Permanent laboratory = transformWithoutCreature();
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, unrelated));

        assertThat(gd.stack).isEmpty();
        assertThat(laboratory.isTransformed()).isTrue();
    }

    @Test
    void transformTargetsOnlyCreatureCardsInYourGraveyard() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        ShivanDragon creature = new ShivanDragon();
        Forest land = new Forest();
        ShivanDragon opposingCreature = new ShivanDragon();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opposingCreature));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Dragon");
        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    private Permanent transformWithoutCreature() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        harness.setGraveyard(player1, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
        return laboratory;
    }

    @Test
    void copiedLeaveTriggerDoesNotTransformTheLandTwice() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        ShivanDragon creature = new ShivanDragon();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == creature)
                .findFirst().orElseThrow();
        Permanent resonator = harness.addToBattlefieldAndReturn(player1, new StrionicResonator());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(resonator), 0, null,
                gd.stack.getFirst().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(laboratory.isTransformed()).isFalse();

        harness.passBothPriorities();

        assertThat(laboratory.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysFourManaToInvestigate() {
        Permanent laboratory = addLaboratory();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findClues(player1)).hasSize(1);
        assertThat(laboratory.isTapped()).isTrue();
    }

    @Test
    void investigatedClueCanBeSacrificedForTwoManaToDrawACard() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 1);
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent clue = findClues(player1).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), 0, null, null);
        assertThat(findClues(player1)).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void removedGraveyardTargetIsNotReturnedOnResolution() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        ShivanDragon creature = new ShivanDragon();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformsAtYourEndStepAfterSacrificingThreeClues() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(laboratory.getCard()).isInstanceOf(HavengulMystery.class);
    }

    @Test
    void transformsBackWhenTheReturnedCreatureLeaves() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        ShivanDragon creature = new ShivanDragon();
        Permanent creaturePermanent = harness.addToBattlefieldAndReturn(player1, creature);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creaturePermanent));
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof ShivanDragon)
                .findFirst()
                .orElseThrow();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));
        harness.passBothPriorities();

        assertThat(laboratory.isTransformed()).isFalse();
        assertThat(laboratory.getCard()).isInstanceOf(HavengulLaboratory.class);
    }

    private Permanent addLaboratory() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new HavengulLaboratory());
        laboratory.setSummoningSick(false);
        return laboratory;
    }

    private void investigate(Permanent laboratory, int count) {
        for (int i = 0; i < count; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(laboratory), 1, null, null);
            harness.passBothPriorities();
            laboratory.untap();
        }
    }

    private void sacrificeClues(Player player, int count) {
        harness.setLibrary(player, List.of(new Forest(), new Forest(), new Forest()));
        for (int i = 0; i < count; i++) {
            Permanent clue = findClues(player).getFirst();
            harness.addMana(player, ManaColor.COLORLESS, 2);
            harness.activateAbility(player,
                    gd.playerBattlefields.get(player.getId()).indexOf(clue), 0, null, null);
            harness.passBothPriorities();
        }
    }

    private List<Permanent> findClues(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CLUE))
                .toList();
    }

}
