package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.b.BottleOfSuleiman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TitaniasSongTest extends BaseCardTest {

    // ===== Animating noncreature artifacts =====

    @Test
    @DisplayName("Noncreature artifact becomes a creature with P/T equal to mana value")
    void animatesNoncreatureArtifact() {
        // Angel's Feather costs {2}, so mana value = 2
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new TitaniasSong());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not animate creatures")
    void doesNotAffectCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TitaniasSong());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Loses all abilities =====

    @Test
    @DisplayName("Animated artifact loses its activated ability")
    void stripsArtifactAbilities() {
        // Bottle of Suleiman's ability has no tap symbol, so any activation failure is
        // due to losing all abilities rather than summoning sickness.
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("March of the Machines animates the same artifact but keeps its ability")
    void marchAnimatesWithoutStrippingAbilities() {
        // Contrast: March of the Machines animates but does NOT remove abilities, so the
        // non-tap ability still works — isolating ability loss as Titania's Song behavior.
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Sacrifice is a cost, so the Bottle leaves the battlefield when the ability is used.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bottle of Suleiman"));
    }

    // ===== Effect ends when Titania's Song leaves =====

    @Test
    @DisplayName("Artifacts revert to non-creatures when Titania's Song leaves")
    void artifactsRevertWhenSongLeaves() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new TitaniasSong());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        assertThat(gqs.isCreature(gd, feather)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Titania's Song"));

        assertThat(gqs.isCreature(gd, feather)).isFalse();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(0);
    }

    // ===== Affects both players' artifacts =====

    @Test
    @DisplayName("Affects artifacts on both sides of the battlefield")
    void affectsBothPlayersArtifacts() {
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addToBattlefield(player2, new AngelsFeather());

        Permanent opponentFeather = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        assertThat(gqs.isCreature(gd, opponentFeather)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentFeather)).isEqualTo(2);
    }
}
