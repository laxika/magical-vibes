package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiredTorturer.class, Forest.class, GrizzlyBears.class})
class HiredTorturerTest extends BaseCardTest {

    private void readyTorturer() {
        addCreatureReady(player1, new HiredTorturer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Target opponent loses 2 life; the revealed card stays in their hand")
    void opponentLosesTwoLifeAndReveals() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        readyTorturer();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty hand still loses 2 life, nothing revealed")
    void emptyHandStillLosesLife() {
        harness.setHand(player2, new ArrayList<>());
        readyTorturer();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetSelf() {
        readyTorturer();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("A one-card hand reveals its only card without moving it")
    void revealsOnlyCardInHand() {
        HiredTorturer card = new HiredTorturer();
        harness.setHand(player2, List.of(card));
        readyTorturer();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Hired Torturer at random.")).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only one random card from a multi-card hand is revealed to both players")
    void revealsExactlyOneCardToBothPlayers() throws Exception {
        HiredTorturer first = new HiredTorturer();
        HiredTorturer second = new HiredTorturer();
        harness.setHand(player2, List.of(first, second));
        readyTorturer();
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        var controllerMessages = harness.getConn1().getMessagesContaining("REVEAL_HAND");
        var opponentMessages = harness.getConn2().getMessagesContaining("REVEAL_HAND");
        assertThat(controllerMessages).hasSize(1);
        assertThat(opponentMessages).hasSize(1);
        var mapper = new JacksonConfig().objectMapper();
        var controllerCards = mapper.readTree(controllerMessages.getFirst()).get("cards");
        var opponentCards = mapper.readTree(opponentMessages.getFirst()).get("cards");
        assertThat(controllerCards.size()).isEqualTo(1);
        assertThat(opponentCards).isEqualTo(controllerCards);
        assertThat(controllerCards.get(0).get("id").asText())
                .isIn(first.getId().toString(), second.getId().toString());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Defender prevents Hired Torturer from attacking")
    void cannotAttack() {
        addCreatureReady(player1, new HiredTorturer());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana and tapping are paid before the opponent loses life")
    void paysCostsBeforeResolution() {
        readyTorturer();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player2, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("A tapped Hired Torturer cannot activate its ability")
    void cannotActivateWhileTapped() {
        readyTorturer();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        readyTorturer();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new HiredTorturer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
