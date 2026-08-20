package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
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

class BergStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature and it untaps normally without snow mana")
    void tapsCreatureWithoutSnowMana() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBergStrider(bears.getId(), false);

        assertThat(bears.isTapped()).isTrue();
        untapPlayer(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps an opponent's artifact and it skips its next untap with snow mana")
    void tapsArtifactAndSkipsUntapWithSnowMana() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

        castBergStrider(ornithopter.getId(), true);

        assertThat(ornithopter.isTapped()).isTrue();
        untapPlayer(player2);

        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BergStrider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature an opponent controls");
    }

    private void castBergStrider(UUID targetId, boolean snowMana) {
        harness.setHand(player1, List.of(new BergStrider()));
        if (snowMana) {
            harness.addMana(player1, ManaColor.COLORLESS, 4);
        } else {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 4);
        }
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void untapPlayer(Player player) {
        harness.forceActivePlayer(player.equals(player1) ? player2 : player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
