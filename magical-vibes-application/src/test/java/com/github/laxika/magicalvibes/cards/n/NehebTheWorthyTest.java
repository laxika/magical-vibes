package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinotaurSureshot;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NehebTheWorthy.class, GrizzlyBears.class, MinotaurSureshot.class})
class NehebTheWorthyTest extends BaseCardTest {

    private Permanent addReadyNeheb() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new NehebTheWorthy());
        perm.setSummoningSick(false);
        return perm;
    }


    @Test
    @DisplayName("Neheb (a Minotaur) gets +2/+0 while its controller has one or fewer cards in hand")
    void selfBoostedWithEmptyHand() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        harness.setHand(player1, new ArrayList<>());

        Permanent neheb = findPermanent(player1, "Neheb, the Worthy");

        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, neheb)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost turns off with two or more cards in hand")
    void notBoostedWithTwoCards() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        Permanent neheb = findPermanent(player1, "Neheb, the Worthy");

        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, neheb)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Minotaur creatures you control are not boosted")
    void nonMinotaurNotBoosted() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }


    @Test
    @DisplayName("Non-Minotaur creatures you control do not gain first strike")
    void nonMinotaurDoesNotGetFirstStrike() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }


    @Test
    @DisplayName("When Neheb deals combat damage to a player, each player discards a card (APNAP)")
    void combatDamageMakesEachPlayerDiscard() {
        Permanent neheb = addReadyNeheb();
        neheb.setAttacking(true);
        // Two cards for the active player so the +2/+0 boost is off and a discard choice exists.
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve the triggered ability

        // APNAP: active player (player1) chooses first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // Then the opponent chooses before both cards are discarded.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("One card enables the boost, and changing hand size updates it immediately")
    void boostTracksOneCardThreshold() {
        Permanent neheb = harness.addToBattlefieldAndReturn(player1, new NehebTheWorthy());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new MinotaurSureshot());
        harness.setHand(player1, List.of(new MinotaurSureshot()));

        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);

        harness.setHand(player1, List.of(new MinotaurSureshot(), new MinotaurSureshot()));
        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only another Minotaur controlled by Neheb's controller receives its bonuses")
    void bonusesDoNotAffectOpponentsMinotaurs() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new MinotaurSureshot());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MinotaurSureshot());
        harness.setHand(player1, List.of());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty controller hand does not prevent the opponent from discarding")
    void emptyControllerHandStillMakesOpponentDiscard() {
        Permanent neheb = addReadyNeheb();
        neheb.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new MinotaurSureshot()));
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Minotaur Sureshot");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Discard choices remain in hand until every player has chosen")
    void discardsOccurSimultaneouslyAfterAllChoices() {
        Permanent neheb = addReadyNeheb();
        neheb.setAttacking(true);
        harness.setHand(player1, List.of(new MinotaurSureshot(), new MinotaurSureshot()));
        harness.setHand(player2, List.of(new MinotaurSureshot(), new MinotaurSureshot()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Minotaur Sureshot");
        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Minotaur Sureshot");
        harness.assertInGraveyard(player2, "Minotaur Sureshot");
        assertThat(gqs.getEffectivePower(gd, neheb)).isEqualTo(4);
    }
}
