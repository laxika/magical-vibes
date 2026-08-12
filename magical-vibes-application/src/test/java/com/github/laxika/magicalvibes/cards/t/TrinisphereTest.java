package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DampingSphere;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrinisphereTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Trinisphere makes a two-mana spell cost three")
    void untappedTrinisphereMakesCheapSpellCostThree() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trinisphere does not increase a spell that already costs three mana")
    void doesNotIncreaseThreeManaSpell() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.setHand(player1, List.of(new DampingSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapped Trinisphere does not increase spell costs")
    void tappedTrinisphereDoesNotIncreaseSpellCosts() {
        harness.addToBattlefield(player1, new Trinisphere());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
