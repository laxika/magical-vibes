package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlBhedSalvagers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SephirothFabledSoldier.class, SephirothOneWingedAngel.class,
        AlBhedSalvagers.class, GrizzlyBears.class, Shock.class})
class SephirothFabledSoldierTest extends BaseCardTest {

    @Test
    void entersAndMaySacrificeAnotherCreatureToDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SephirothFabledSoldier(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void fourthResolvedDeathTriggerTransformsAndCreatesDeathEmblem() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothFabledSoldier());
        sephiroth.setSummoningSick(false);
        List<Permanent> bears = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));

        for (int i = 0; i < 4; i++) {
            destroyWithShock(bears.get(i));
        }
        harness.passBothPriorities();

        assertThat(sephiroth.isTransformed())
                .as("resolution counts=%s, player1 life=%s, player2 life=%s, stack=%s, pending=%s",
                        gd.permanentAbilityResolutionsThisTurn,
                        gd.getLife(player1.getId()), gd.getLife(player2.getId()), gd.stack,
                        gd.pendingInteractions)
                .isTrue();
        assertThat(sephiroth.getCard()).isInstanceOf(SephirothOneWingedAngel.class);
        assertThat(gd.emblems).hasSize(1);

        destroyWithShock(bears.get(4));

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
    }

    @Test
    void oneWingedAngelMaySacrificeAnyNumberOfOtherCreaturesToDrawThatMany() {
        SephirothFabledSoldier front = new SephirothFabledSoldier();
        Permanent sephiroth = new Permanent(front);
        sephiroth.setCard(front.getBackFaceCard());
        sephiroth.setTransformed(true);
        sephiroth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sephiroth);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .filteredOn(name -> name.equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void emblemExistsImmediatelyWhenFourthDeathAbilityResolves() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothFabledSoldier());
        for (int i = 0; i < 4; i++) {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            destroyWithShock(bear);
        }

        assertThat(sephiroth.isTransformed()).isTrue();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void decliningEntrySacrificeDoesNotDrawOrDrain() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SephirothFabledSoldier(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void frontFaceAttackCanSacrificeAnotherCreatureAndDraw() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothFabledSoldier());
        sephiroth.setSummoningSick(false);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(sephiroth.isTransformed()).isFalse();
    }

    @Test
    void backFaceAttackCanChooseZeroSacrifices() {
        SephirothFabledSoldier front = new SephirothFabledSoldier();
        Permanent sephiroth = new Permanent(front);
        sephiroth.setCard(front.getBackFaceCard());
        sephiroth.setTransformed(true);
        sephiroth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sephiroth);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.emblems).isEmpty();
    }

    @Test
    void backFaceSacrificesChosenCreaturesSimultaneously() {
        SephirothFabledSoldier front = new SephirothFabledSoldier();
        Permanent sephiroth = new Permanent(front);
        sephiroth.setCard(front.getBackFaceCard());
        sephiroth.setTransformed(true);
        sephiroth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sephiroth);
        Permanent salvagers = harness.addToBattlefieldAndReturn(player1, new AlBhedSalvagers());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(salvagers.getId(), bear.getId()));

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Al Bhed Salvagers");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void frontFaceDoesNotDrainForItsOwnDeath() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothFabledSoldier());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 2; i++) {
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.clearPriorityPassed();
            harness.castInstant(player2, 0, sephiroth.getId());
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Sephiroth, Fabled SOLDIER");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
