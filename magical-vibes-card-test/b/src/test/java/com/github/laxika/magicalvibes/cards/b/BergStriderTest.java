package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AxgardBraggart;
import com.github.laxika.magicalvibes.cards.r.RavenWings;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BergStrider.class, AxgardBraggart.class, RavenWings.class, SnowCoveredIsland.class})
class BergStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature and it untaps normally without snow mana")
    void tapsCreatureWithoutSnowMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxgardBraggart());

        castBergStrider(creature.getId(), false);

        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps an opponent's artifact and it skips its next untap with snow mana")
    void tapsArtifactAndSkipsUntapWithSnowMana() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RavenWings());
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

        castBergStrider(artifact.getId(), true);

        assertThat(artifact.isTapped()).isTrue();
        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardBraggart());
        harness.setHand(player1, List.of(new BergStrider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("An already tapped creature still skips untap, even after Berg Strider leaves")
    void locksAlreadyTappedCreatureAfterSourceLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxgardBraggart());
        creature.tap();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

        castBergStrider(creature.getId(), true);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof BergStrider);

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land that is neither an artifact nor a creature")
    void cannotTargetOrdinaryLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new BergStrider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
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

}
