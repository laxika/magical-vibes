package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedemptorDreadnought.class, GrizzlyBears.class})
class RedemptorDreadnoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card makes the attack trigger use its power")
    void boostsByPowerOfExiledCreature() {
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledCreature));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(6);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack trigger does nothing when no card was exiled")
    void doesNothingWithoutExiledCard() {
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(4);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The additional cost accepts at most one creature card")
    void rejectsMoreThanOneExiledCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 1");
    }
}
