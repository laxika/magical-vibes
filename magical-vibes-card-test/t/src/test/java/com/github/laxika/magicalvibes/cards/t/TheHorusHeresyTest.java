package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHorusHeresy.class, GrizzlyBears.class, MirriCatWarrior.class, Naturalize.class})
class TheHorusHeresyTest extends BaseCardTest {

    @Test
    void chapterISeizesOneNonlegendaryCreatureUntilTheSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId()).doesNotContain(legendaryCreature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, saga.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void chapterIIDrawsForCreaturesYouControlButDoNotOwn() {
        addSagaWithLore(0);
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, stolenCreature.getId());
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        triggerChapter();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenCreature);
    }

    @Test
    void chapterIIIEachPlayerChoosesAcreatureAndAllChosenCreaturesAreDestroyed() {
        Permanent playerOneChosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent playerOneSurvivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent playerTwoChosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent playerTwoSurvivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice playerOneChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerOneChoice.validPermanentIds())
                .containsExactlyInAnyOrder(playerOneChosen.getId(), playerOneSurvivor.getId(),
                        playerTwoChosen.getId(), playerTwoSurvivor.getId());
        harness.handlePermanentChosen(player1, playerTwoChosen.getId());

        PendingInteraction.PermanentChoice playerTwoChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerTwoChoice.validPermanentIds())
                .containsExactlyInAnyOrder(playerOneChosen.getId(), playerOneSurvivor.getId(),
                        playerTwoChosen.getId(), playerTwoSurvivor.getId());
        harness.handlePermanentChosen(player2, playerOneChosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(playerOneSurvivor).doesNotContain(playerOneChosen);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(playerTwoSurvivor).doesNotContain(playerTwoChosen);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void chapterIIIBothPlayersMayChooseTheSameCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownSurvivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSurvivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validPermanentIds()).contains(chosen.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen);
        harness.handlePermanentChosen(player2, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownSurvivor).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentSurvivor).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
    }

    @Test
    void chapterIIIPlayerWithoutCreaturesStillChoosesAnOpponentsCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player2, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
    }

    @Test
    void chapterIIDoesNotDrawForCreaturesYouOwn() {
        addSagaWithLore(1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIMayChooseNoCreature() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void chapterIDoesNotGainControlIfSagaLeavesBeforeResolution() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, saga.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Horus Heresy");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheHorusHeresy());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
