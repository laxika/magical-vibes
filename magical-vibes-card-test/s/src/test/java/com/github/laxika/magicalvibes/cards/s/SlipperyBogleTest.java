package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipperyBogle.class, Shock.class, GiantGrowth.class, ProdigalPyromancer.class})
class SlipperyBogleTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Slippery Bogle with spells")
    void opponentCannotTargetWithSpells() {
        Permanent boglePerm = addCreatureReady(player1, new SlipperyBogle());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, boglePerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Slippery Bogle with spells")
    void controllerCanTargetOwnBogle() {
        Permanent boglePerm = addCreatureReady(player1, new SlipperyBogle());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, boglePerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Opponent cannot target Slippery Bogle with activated abilities")
    void opponentCannotTargetWithAbilities() {
        Permanent bogle = addCreatureReady(player1, new SlipperyBogle());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bogle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(pyromancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller can target and destroy own Slippery Bogle with an activated ability")
    void controllerCanTargetOwnBogleWithAbility() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bogle = addCreatureReady(player1, new SlipperyBogle());

        harness.activateAbility(player1, 0, null, bogle.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slippery Bogle");
        harness.assertNotOnBattlefield(player1, "Slippery Bogle");
    }
}
