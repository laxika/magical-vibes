package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JasperaSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and another creature, then adds one mana of the chosen color")
    void tapsItselfAndAnotherCreatureForMana() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, sentinelIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prompts which other creature to tap when multiple are available")
    void promptsForCreatureChoice() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());

        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, sentinelIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstBears.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(firstBears.isTapped()).isTrue();
        assertThat(secondBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature")
    void cannotActivateWithoutAnotherUntappedCreature() {
        Permanent sentinel = addCreatureReady(player1, new JasperaSentinel());
        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);

        assertThatThrownBy(() -> harness.activateAbility(player1, sentinelIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }
}
