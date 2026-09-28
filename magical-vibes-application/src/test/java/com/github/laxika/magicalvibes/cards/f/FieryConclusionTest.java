package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.g.GreaterMossdog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryConclusion.class, GreaterMossdog.class, BorosSignet.class})
class FieryConclusionTest extends BaseCardTest {

    @Test
    @DisplayName("Fiery Conclusion sacrifices a creature and deals 5 damage to the target creature")
    void sacrificesAndDealsFiveDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());

        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Greater Mossdog");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greater Mossdog");
        harness.assertInGraveyard(player2, "Greater Mossdog");
        harness.assertInGraveyard(player1, "Fiery Conclusion");
    }

    @Test
    @DisplayName("A creature with more than 5 toughness survives Fiery Conclusion")
    void largeCreatureSurvives() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());
        target.setToughnessModifier(4);

        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot cast Fiery Conclusion without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());

        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Fiery Conclusion")
    void cannotTargetNonCreaturePermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    @Test
    @DisplayName("Fiery Conclusion fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreaterMossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterMossdog());

        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greater Mossdog");
        harness.assertInGraveyard(player1, "Fiery Conclusion");
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
