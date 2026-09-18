package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidssFrightfulReturn.class, GrizzlyBears.class, LotusPetal.class, Shock.class})
class BraidssFrightfulReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I sacrifices a creature and makes each opponent discard")
    void chapterISacrificesCreatureAndMakesOpponentsDiscard() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }

    @Test
    @DisplayName("Declining chapter I leaves the creature and opponent's hand unchanged")
    void chapterIDeclineDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock retained = new Shock();
        harness.setHand(player2, List.of(retained));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("Chapter II returns a target creature card from the graveyard to hand")
    void chapterIIReturnsCreatureToHand() {
        GrizzlyBears returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        addSagaWithLore(1);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III lets the targeted opponent sacrifice a nonland nontoken permanent")
    void chapterIIIOpponentSacrificesPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LotusPetal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Lotus Petal");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining chapter III causes the targeted opponent to lose life and the controller to draw")
    void chapterIIIDeclineCausesLifeLossAndDraw() {
        harness.setHand(player1, List.of());
        Shock drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new LotusPetal());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(permanent);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BraidssFrightfulReturn());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
