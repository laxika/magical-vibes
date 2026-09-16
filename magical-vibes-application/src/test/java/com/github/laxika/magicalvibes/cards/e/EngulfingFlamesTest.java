package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkwaterEgg;
import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EngulfingFlames.class, DwarvenGrunt.class, EmberBeast.class, DarkwaterEgg.class})
class EngulfingFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and prevents regeneration of the target creature")
    void dealsDamageAndPreventsRegeneration() {
        Permanent grunt = addCreatureReady(player2, new DwarvenGrunt());
        grunt.setRegenerationShield(1);
        harness.setHand(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, grunt.getId());

        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        harness.assertInGraveyard(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Flashback exiles Engulfing Flames after resolving")
    void flashbackExilesAfterResolving() {
        Permanent emberBeast = harness.addToBattlefieldAndReturn(player2, new EmberBeast());
        harness.setGraveyard(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, emberBeast.getId());

        harness.assertOnBattlefield(player2, "Ember Beast");
        assertThat(emberBeast.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Engulfing Flames");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Engulfing Flames"));
    }

    @Test
    @DisplayName("Flashback exiles Engulfing Flames when its target is gone")
    void flashbackExilesWhenTargetLeavesBeforeResolution() {
        Permanent emberBeast = harness.addToBattlefieldAndReturn(player2, new EmberBeast());
        harness.setGraveyard(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, emberBeast.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Engulfing Flames");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Engulfing Flames"));
    }

    @Test
    @DisplayName("Cannot cast Engulfing Flames with flashback without its full cost")
    void flashbackFailsWithoutEnoughMana() {
        Permanent emberBeast = harness.addToBattlefieldAndReturn(player2, new EmberBeast());
        harness.setGraveyard(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, emberBeast.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent egg = harness.addToBattlefieldAndReturn(player2, new DarkwaterEgg());
        harness.setHand(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, egg.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
