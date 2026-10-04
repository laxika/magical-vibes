package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fragmentize.class, AngelicChorus.class, GloriousAnthem.class, GrizzlyBears.class,
        HowlingMine.class, JayemdaeTome.class, Ornithopter.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    @Test
    @DisplayName("Destroys an artifact with mana value exactly four")
    void destroysArtifactAtManaValueLimit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());

        castFragmentize(target.getId());

        harness.assertNotOnBattlefield(player2, "Jayemdae Tome");
        harness.assertInGraveyard(player2, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Destroys a zero-cost artifact creature")
    void destroysZeroCostArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castFragmentize(target.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its caster")
    void destroysOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        castFragmentize(target.getId());

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Howling Mine");
    }

    private void castFragmentize(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Fragmentize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
