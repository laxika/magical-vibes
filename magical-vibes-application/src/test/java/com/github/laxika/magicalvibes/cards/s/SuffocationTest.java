package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuffocationTest extends BaseCardTest {

    /** Player 2 bolts player 1 for 3, enabling Suffocation. */
    private void boltPlayerOne() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 4 damage to the controller of the red spell that damaged you")
    void dealsFourToRedSpellController() {
        boltPlayerOne();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Suffocation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 4);
    }

    @Test
    @DisplayName("Schedules a draw for its controller at the next upkeep")
    void schedulesDrawAtNextUpkeep() {
        boltPlayerOne();

        harness.setHand(player1, List.of(new Suffocation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Not castable when no red instant or sorcery damaged you this turn")
    void notCastableWithoutRedSpellDamage() {
        harness.setHand(player1, List.of(new Suffocation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Damage dealt to an opponent by your own red spell does not enable the cast")
    void damageToOpponentDoesNotEnableCast() {
        harness.setHand(player1, List.of(new LightningBolt(), new Suffocation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Damage from a non-spell source does not enable the cast")
    void nonSpellDamageDoesNotEnableCast() {
        harness.setHand(player1, List.of(new Suffocation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.recordDamageToPlayer(player1.getId(), 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Hits the controller of the most recent red spell that damaged you")
    void hitsMostRecentRedSpellController() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        boltPlayerOne();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        int ownLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Suffocation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLife);
    }
}
