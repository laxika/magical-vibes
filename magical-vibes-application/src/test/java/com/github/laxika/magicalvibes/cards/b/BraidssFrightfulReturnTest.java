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

    @Test
    @DisplayName("Read ahead lets the controller start at chapter III and skip earlier chapters")
    void readAheadStartsAtChapterThree() {
        harness.setLibrary(player1, List.of(new BraidssFrightfulReturn()));
        harness.castFromHand(player1, new BraidssFrightfulReturn(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Braids's Frightful Return");
        harness.assertInGraveyard(player1, "Braids's Frightful Return");
        harness.assertNotOnBattlefield(player1, "Braids's Frightful Return");
    }

    @Test
    @DisplayName("Chapter I cannot make opponents discard when no creature is sacrificed")
    void chapterIWithoutCreatureDoesNotDiscard() {
        Shock retained = new Shock();
        harness.setHand(player2, List.of(retained));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter II does not return a creature card that leaves the graveyard before resolution")
    void chapterIITargetLeavesGraveyard() {
        GrizzlyBears targeted = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targeted));
        addSagaWithLore(1);

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(targeted.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(targeted);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter III causes life loss and a draw when the opponent has nothing to sacrifice")
    void chapterIIIWithoutEligiblePermanentCausesLifeLossAndDraw() {
        harness.setHand(player1, List.of());
        BraidssFrightfulReturn drawn = new BraidssFrightfulReturn();
        harness.setLibrary(player1, List.of(drawn));
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Braids's Frightful Return");
    }

    @Test
    @DisplayName("Chapter II can target only creature cards in its controller's graveyard")
    void chapterIITargetsOnlyOwnCreatureCards() {
        GrizzlyBears eligible = new GrizzlyBears();
        GrizzlyBears opposingCreature = new GrizzlyBears();
        Shock noncreature = new Shock();
        harness.setGraveyard(player1, List.of(eligible, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        addSagaWithLore(1);

        triggerNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(eligible);
        assertThat(choice.minCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(eligible);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BraidssFrightfulReturn());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
