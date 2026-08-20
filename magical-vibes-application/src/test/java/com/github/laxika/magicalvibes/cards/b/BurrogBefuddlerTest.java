package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurrogBefuddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurrogBefuddler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB gives an opponent's creature -1/-0 until end of turn")
    void weakensOpponentCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BurrogBefuddler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, targetId);
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BurrogBefuddler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findPermanent(Player player, UUID id) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
