package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Amok.class, VenerableMonk.class})
class AmokTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a +1/+1 counter on target creature and discards a card at random as a cost")
    void putsCounterOnTargetAndDiscardsAtRandom() {
        harness.addToBattlefield(player1, new Amok());
        Permanent monk = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new VenerableMonk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID monkId = monk.getId();
        harness.activateAbility(player1, battlefieldIndex(player1, "Amok"), null, monkId);
        harness.passBothPriorities();

        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Venerable Monk");
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new Amok());
        Permanent monk = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID monkId = monk.getId();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Amok"), null, monkId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new Amok());
        Permanent monk = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new VenerableMonk()));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Amok"), null, monk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Amok());
        harness.setHand(player1, List.of(new VenerableMonk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID amokId = harness.getPermanentId(player1, "Amok");
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Amok"), null, amokId))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, cardName));
    }
}
