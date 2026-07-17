package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtheriumSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells cost {1} less to cast with Etherium Sculptor on the battlefield")
    void artifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EtheriumSculptor());
        // Sylvok Lifestaff costs {1} — with {1} reduction it should cost {0}
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Two Etherium Sculptors reduce artifact cost by {2}")
    void twoSculptorsStackReduction() {
        harness.addToBattlefield(player1, new EtheriumSculptor());
        harness.addToBattlefield(player1, new EtheriumSculptor());
        // Sylvok Lifestaff costs {1} — generic cost cannot go below {0}
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Non-artifact spells are not reduced by Etherium Sculptor")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new EtheriumSculptor());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Etherium Sculptor does not reduce opponent's artifact spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new EtheriumSculptor());
        // Opponent's Sylvok Lifestaff should still cost {1}
        harness.setHand(player2, List.of(new SylvokLifestaff()));

        // No mana is not enough for {1} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
