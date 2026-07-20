package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class NehebTheWorthyTest extends BaseCardTest {

    private Permanent addReadyNeheb() {
        Permanent perm = new Permanent(new NehebTheWorthy());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Static: Minotaurs you control get +2/+0 while you have one or fewer cards in hand =====

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

    // ===== Static: only Minotaurs you control get first strike =====

    @Test
    @DisplayName("Non-Minotaur creatures you control do not gain first strike")
    void nonMinotaurDoesNotGetFirstStrike() {
        harness.addToBattlefield(player1, new NehebTheWorthy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Trigger: combat damage to a player makes each player discard =====

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

        // APNAP: active player (player1) discards first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // Then the opponent discards.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
