package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FractureTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castFracture(target);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntangibleVirtue());

        castFracture(target);

        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        harness.assertInGraveyard(player2, "Intangible Virtue");
    }

    @Test
    @DisplayName("Destroys a target planeswalker")
    void destroysPlaneswalker() {
        Permanent target = new Permanent(new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(target);

        castFracture(target);

        harness.assertNotOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        harness.assertInGraveyard(player2, "Nicol Bolas, Planeswalker");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fracture()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or planeswalker");
    }

    private void castFracture(Permanent target) {
        harness.setHand(player1, List.of(new Fracture()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
