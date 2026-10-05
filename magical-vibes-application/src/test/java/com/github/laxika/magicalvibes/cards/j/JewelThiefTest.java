package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@CardUsed(JewelThief.class)
class JewelThiefTest extends BaseCardTest {

    @Test
    @DisplayName("When Jewel Thief enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void treasureIsCreatedOnlyWhenTheEnterTriggerResolves() {
        harness.setHand(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jewel Thief");
        assertThat(countPermanents(player1, "Treasure")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void enterTriggerStillCreatesTreasureAfterSourceDies() {
        Permanent thief = harness.enterBattlefieldAndReturn(player1, new JewelThief());
        thief.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Jewel Thief");

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void enteringWithoutBeingCastCreatesTreasureForItsController() {
        harness.enterBattlefieldAndReturn(player2, new JewelThief());

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        harness.enterBattlefieldAndReturn(player1, new JewelThief());
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingDoesNotTapJewelThief() {
        Permanent thief = addCreatureReady(player1, new JewelThief());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(thief.isTapped()).isFalse();
    }
}
