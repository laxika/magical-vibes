package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NullifyTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Nullify");
    }

    @Test
    @DisplayName("Counters an Aura spell")
    void countersAuraSpell() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();

        AbundantGrowth growth = new AbundantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, growth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertNotOnBattlefield(player1, "Abundant Growth");
    }

    @Test
    @DisplayName("Cannot target a non-Aura enchantment spell")
    void cannotTargetNonAuraEnchantmentSpell() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setHand(player1, List.of(chorus));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, chorus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
