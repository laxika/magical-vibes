package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoyagerQuickwelderTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells cost {1} less to cast")
    void artifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new VoyagerQuickwelder());
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Non-artifact spells are not reduced")
    void nonArtifactSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new VoyagerQuickwelder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's artifact spells")
    void opponentArtifactSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new VoyagerQuickwelder());
        harness.setHand(player2, List.of(new SylvokLifestaff()));

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
