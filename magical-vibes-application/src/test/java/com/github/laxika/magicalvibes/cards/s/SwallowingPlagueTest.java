package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.k.KitsuneBlademaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowingPlague.class, KitsuneBlademaster.class, HondenOfSeeingWinds.class})
class SwallowingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Swallowing Plague deals X damage to the target creature and gains X life")
    void dealsXDamageAndGainsXLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kitsune Blademaster");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Life gain happens even when X is too small to kill the creature")
    void gainsLifeWhenCreatureSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kitsune Blademaster");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("X of 0 deals no damage and gains no life")
    void xZeroDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBlademaster());
        harness.setHand(player1, List.of(new SwallowingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kitsune Blademaster");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
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
}
