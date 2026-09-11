package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnthusiasticMechanaut.class, JhoirasFamiliar.class, GrizzlyBears.class})
class EnthusiasticMechanautTest extends BaseCardTest {

    @Test
    void reducesArtifactSpellCosts() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceNonartifactSpellCosts() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyReducesArtifactSpellsCastByController() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player2, List.of(new JhoirasFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
