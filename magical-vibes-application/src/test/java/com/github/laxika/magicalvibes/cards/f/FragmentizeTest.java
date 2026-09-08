package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FragmentizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact with mana value 4 or less")
    void destroysArtifactWithinManaValueLimit() {
        harness.addToBattlefield(player2, new HowlingMine());
        castFragmentize(harness.getPermanentId(player2, "Howling Mine"));

        harness.assertInGraveyard(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Destroys a target enchantment with mana value 4 or less")
    void destroysEnchantmentWithinManaValueLimit() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castFragmentize(harness.getPermanentId(player2, "Glorious Anthem"));

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Rejects a target with the wrong type or mana value")
    void rejectsIllegalTargets() {
        Permanent creature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(creature);
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new Fragmentize()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");

        assertThatThrownBy(() -> harness.castSorcery(
                player1,
                0,
                harness.getPermanentId(player2, "Angelic Chorus")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 4 or less");
    }

    private void castFragmentize(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Fragmentize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
