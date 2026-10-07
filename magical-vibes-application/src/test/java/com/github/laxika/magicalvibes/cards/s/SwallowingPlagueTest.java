package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandlesGlow;
import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.k.KitsuneBlademaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowingPlague.class, KitsuneBlademaster.class, HondenOfSeeingWinds.class,
        CandlesGlow.class, ConsumingVortex.class})
class SwallowingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Swallowing Plague deals X damage to the target creature and gains X life")
    void dealsXDamageAndGainsXLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        harness.assertInGraveyard(player2, "Kitsune Blademaster");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Life gain happens even when X is too small to kill the creature")
    void gainsLifeWhenCreatureSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        harness.assertOnBattlefield(player2, "Kitsune Blademaster");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("X of 0 deals no damage and gains no life")
    void xZeroDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kitsune Blademaster");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HondenOfSeeingWinds());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsChosenXLifeWhenAllDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setHand(player2, List.of(new CandlesGlow()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 3, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kitsune Blademaster");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player1, "Swallowing Plague");
    }

    @Test
    void gainsNoLifeWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 3, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kitsune Blademaster");
        harness.assertNotOnBattlefield(player2, "Kitsune Blademaster");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Swallowing Plague");
    }

    @Test
    void canTargetOwnCreatureAndGainLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertInGraveyard(player1, "Kitsune Blademaster");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
