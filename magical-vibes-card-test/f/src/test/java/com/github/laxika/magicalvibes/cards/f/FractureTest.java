package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fracture.class, GrizzlyBears.class, IntangibleVirtue.class, Millstone.class,
        NicolBolasPlaneswalker.class, CampusGuide.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);

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

    @Test
    @DisplayName("Can destroy an artifact even when it is also a creature")
    void destroysArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CampusGuide());

        castFracture(target);

        harness.assertNotOnBattlefield(player2, "Campus Guide");
        harness.assertInGraveyard(player2, "Campus Guide");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by its caster")
    void destroysOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());

        castFracture(target);

        harness.assertNotOnBattlefield(player1, "Campus Guide");
        harness.assertInGraveyard(player1, "Campus Guide");
    }

    @Test
    @DisplayName("Does not destroy another artifact when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        harness.addToBattlefield(player2, new CampusGuide());
        harness.setHand(player1, List.of(new Fracture(), new Fracture()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Campus Guide");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void castFracture(Permanent target) {
        harness.setHand(player1, List.of(new Fracture()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
