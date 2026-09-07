package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturalState.class, HowlingMine.class, GloriousAnthem.class, IcyManipulator.class, GrizzlyBears.class})
class NaturalStateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact with mana value 3 or less")
    void destroysArtifactWithinManaValueLimit() {
        harness.addToBattlefield(player2, new HowlingMine());
        UUID targetId = harness.getPermanentId(player2, "Howling Mine");

        castNaturalState(targetId);

        harness.assertInGraveyard(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Destroys a target enchantment with mana value 3 or less")
    void destroysEnchantmentWithinManaValueLimit() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");

        castNaturalState(targetId);

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Rejects targets with the wrong type or mana value")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player2, new IcyManipulator());
        UUID highManaValueTargetId = harness.getPermanentId(player2, "Icy Manipulator");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID wrongTypeTargetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new NaturalState()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, highManaValueTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wrongTypeTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void castNaturalState(UUID targetId) {
        harness.setHand(player1, List.of(new NaturalState()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
