package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SigilBlessingTest extends BaseCardTest {

    private void addManaCost(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Target creature you control gets +3/+3, other own creatures get +1/+1")
    void boostsTargetAndOthers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        List<Permanent> bears = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID targetId = bears.getFirst().getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = bears.getFirst();
        Permanent other = bears.get(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent opponentBear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        List<Permanent> bears = harness.getGameData().playerBattlefields.get(player1.getId());
        harness.castInstant(player1, 0, bears.getFirst().getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(bears.getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.get(1).getEffectivePower()).isEqualTo(2);
        assertThat(bears.get(1).getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // legal target so the spell is castable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
