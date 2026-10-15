package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.h.HeadlongRush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Douse.class, HeadlongRush.class, CoralMerfolk.class})
class DouseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a red spell")
    void countersRedSpell() {
        HeadlongRush rush = new HeadlongRush();
        var douse = harness.addToBattlefieldAndReturn(player1, new Douse());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, rush, "{1}{R}");
        harness.activateAbility(player1, 0, null, rush.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(douse.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Headlong Rush");
    }

    @Test
    @DisplayName("Cannot target a non-red spell")
    void cannotTargetNonRedSpell() {
        CoralMerfolk merfolk = new CoralMerfolk();

        harness.addToBattlefield(player1, new Douse());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, merfolk, "{1}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Can counter its controller's red spell")
    void countersOwnRedSpell() {
        HeadlongRush rush = new HeadlongRush();
        harness.addToBattlefield(player1, new Douse());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, rush, "{1}{R}");

        harness.activateAbility(player1, 0, null, rush.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Headlong Rush");
        harness.assertOnBattlefield(player1, "Douse");
    }

    @Test
    @DisplayName("Can activate twice targeting the same red spell")
    void canActivateTwiceForSameSpell() {
        HeadlongRush rush = new HeadlongRush();
        var douse = harness.addToBattlefieldAndReturn(player1, new Douse());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player2, rush, "{1}{R}");

        harness.activateAbility(player1, 0, null, rush.getId());
        harness.activateAbility(player1, 0, null, rush.getId());
        assertThat(gd.stack).hasSize(3);
        assertThat(douse.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Headlong Rush");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Douse");
    }

    @Test
    @DisplayName("Cannot activate without the blue mana in its cost")
    void cannotActivateWithoutBlueMana() {
        HeadlongRush rush = new HeadlongRush();
        harness.addToBattlefield(player1, new Douse());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, rush, "{1}{R}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rush.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Headlong Rush");
    }
}
