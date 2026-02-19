package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoriaWurmTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Scoria Wurm has correct card properties")
    void hasCorrectProperties() {
        ScoriaWurm card = new ScoriaWurm();

        assertThat(card.getName()).isEqualTo("Scoria Wurm");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(7);
        assertThat(card.getToughness()).isEqualTo(7);
        assertThat(card.getSubtypes()).contains(CardSubtype.WURM);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ReturnSelfToHandOnCoinFlipLossEffect.class);
    }

    @Test
    @DisplayName("Triggers during controller upkeep")
    void triggersDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new ScoriaWurm());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Scoria Wurm's upkeep ability");
    }

    @Test
    @DisplayName("Does not trigger during opponent upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ScoriaWurm());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Upkeep resolution flips a coin and Scoria Wurm ends in exactly one legal zone")
    void upkeepResolutionFlipsCoinAndMovesOrStays() {
        harness.addToBattlefield(player1, new ScoriaWurm());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Scoria Wurm"));
        boolean inHand = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Scoria Wurm"));

        assertThat(onBattlefield != inHand).isTrue();
        if (inHand) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("returned to its owner's hand"));
        } else {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        }

        assertThat(gd.gameLog)
                .anyMatch(log -> log.contains("coin flip for Scoria Wurm"));
    }
}
