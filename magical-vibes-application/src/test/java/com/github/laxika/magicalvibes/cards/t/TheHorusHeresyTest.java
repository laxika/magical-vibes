package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JodahTheUnifier;
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

@CardUsed({TheHorusHeresy.class, GrizzlyBears.class, JodahTheUnifier.class, Naturalize.class})
class TheHorusHeresyTest extends BaseCardTest {

    @Test
    void chapterISeizesOneNonlegendaryCreatureUntilTheSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player2, new JodahTheUnifier());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId()).doesNotContain(legendaryCreature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void chapterIIDrawsForCreaturesYouControlButDoNotOwn() {
        addSagaWithLore(0);
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, stolenCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
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

        PendingInteraction.PermanentChoice playerOneChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerOneChoice.validPermanentIds())
                .containsExactlyInAnyOrder(playerOneChosen.getId(), playerOneSurvivor.getId());
        harness.handlePermanentChosen(player1, playerOneChosen.getId());

        PendingInteraction.PermanentChoice playerTwoChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerTwoChoice.validPermanentIds())
                .containsExactlyInAnyOrder(playerTwoChosen.getId(), playerTwoSurvivor.getId());
        harness.handlePermanentChosen(player2, playerTwoChosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(playerOneSurvivor).doesNotContain(playerOneChosen);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(playerTwoSurvivor).doesNotContain(playerTwoChosen);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
