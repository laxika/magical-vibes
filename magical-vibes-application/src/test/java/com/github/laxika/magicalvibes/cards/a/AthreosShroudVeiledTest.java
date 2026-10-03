package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.cards.k.KederektParasite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AthreosShroudVeiled.class, Auramancer.class, BurnishedHart.class, InfernalGrasp.class, KederektParasite.class})
class AthreosShroudVeiledTest extends BaseCardTest {

    @Test
    void isNotCreatureBelowDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    void becomesCreatureAtDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);

        assertThat(gqs.isCreature(gd, athreos)).isTrue();
    }

    @Test
    void putsACoinCounterOnAnotherTargetCreatureAtEndStep() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hart.getId());
        harness.passBothPriorities();

        assertThat(hart.getCounterCount(CounterType.COIN)).isEqualTo(1);
    }

    @Test
    void returnsCounteredCreatureFromGraveyardUnderItsControllersControl() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hart));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burnished Hart");
        harness.assertNotInGraveyard(player2, "Burnished Hart");
    }

    @Test
    void returnsCounteredCreatureFromExileUnderItsControllersControl() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, hart));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burnished Hart");
        assertThat(gd.findExiledCard(hart.getCard().getId())).isNull();
    }

    @Test
    void doesNotReturnCreatureWithoutACoinCounter() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hart));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burnished Hart");
    }

    @Test
    void returnsItselfWhenACreatureWithACoinCounterDies() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);
        athreos.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, athreos));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Athreos, Shroud-Veiled");
        harness.assertNotInGraveyard(player1, "Athreos, Shroud-Veiled");
    }

    @Test
    void returnsItselfWhenACreatureWithACoinCounterIsExiled() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);
        athreos.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, athreos));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Athreos, Shroud-Veiled");
        assertThat(gd.findExiledCard(athreos.getCard().getId())).isNull();
    }

    @Test
    void doesNotReturnACardThatLeftExileAndWasExiledAgain() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        BurnishedHart card = new BurnishedHart();
        Permanent hart = harness.addToBattlefieldAndReturn(player2, card);
        hart.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, hart));
        harness.inMutationScope(() -> gd.removeFromExile(card.getId()));
        Permanent returned = harness.addToBattlefieldAndReturn(player2, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, returned));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Burnished Hart");
        harness.assertNotOnBattlefield(player2, "Burnished Hart");
    }

    @Test
    void returnedCreatureHasNoCoinCounterAndDoesNotReturnASecondTime() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.COIN, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hart));
        harness.passBothPriorities();
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Burnished Hart"));
        assertThat(returned.getCounterCount(CounterType.COIN)).isZero();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burnished Hart");
        harness.assertNotOnBattlefield(player1, "Burnished Hart");
    }

    @Test
    void pendingReturnStillResolvesAfterAthreosLeavesTheBattlefield() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hart));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, athreos));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burnished Hart");
        harness.assertNotInGraveyard(player2, "Burnished Hart");
    }

    @Test
    void doesNotReturnCounteredAthreosWhenItIsNotACreature() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        athreos.setCounterCount(CounterType.COIN, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, athreos));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Athreos, Shroud-Veiled");
        assertThat(gd.findExiledCard(athreos.getCard().getId())).isNotNull();
    }

    @Test
    void losesCreatureTypeWhenDevotionFallsBelowSeven() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);
        Permanent parasite = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Kederekt Parasite"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, parasite));

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    void doesNotPutACoinCounterDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hart.getCounterCount(CounterType.COIN)).isZero();
    }

    @Test
    void indestructiblePreventsDestructionWhileAthreosIsACreature() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, athreos.getId());

        harness.assertOnBattlefield(player1, "Athreos, Shroud-Veiled");
        harness.assertNotInGraveyard(player1, "Athreos, Shroud-Veiled");
    }

    @Test
    void countsWhiteAndBlackDevotionTogether() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(2);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Auramancer());
        }

        assertThat(gqs.isCreature(gd, athreos)).isTrue();
    }

    @Test
    void doesNotCountOpponentsPermanentsForDevotion() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(4);
        harness.addToBattlefield(player2, new KederektParasite());

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    void endStepTargetChoicesExcludeAthreosEvenWhenItIsACreature() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosShroudVeiled());
        addBlackPermanents(5);
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(hart.getId()).doesNotContain(athreos.getId());
        harness.handlePermanentChosen(player1, hart.getId());
        harness.passBothPriorities();
        assertThat(hart.getCounterCount(CounterType.COIN)).isEqualTo(1);
        assertThat(athreos.getCounterCount(CounterType.COIN)).isZero();
    }

    @Test
    void doesNotPutCoinCounterOnTargetThatLeftBeforeResolution() {
        harness.addToBattlefield(player1, new AthreosShroudVeiled());
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, hart.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hart));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burnished Hart");
        harness.assertNotOnBattlefield(player1, "Burnished Hart");
        assertThat(hart.getCounterCount(CounterType.COIN)).isZero();
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new KederektParasite());
        }
    }
}
