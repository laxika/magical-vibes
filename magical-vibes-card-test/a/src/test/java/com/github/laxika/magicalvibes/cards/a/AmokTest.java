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

    @Test
    @DisplayName("Random discard is paid immediately while the counter waits for resolution")
    void discardsExactlyOneCardBeforeResolution() {
        harness.addToBattlefield(player1, new Amok());
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        VenerableMonk first = new VenerableMonk();
        Amok second = new Amok();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Amok"), null, monk.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Amok can be activated repeatedly to put counters on your own creature")
    void repeatedActivationsAccumulateCounters() {
        harness.addToBattlefield(player1, new Amok());
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new VenerableMonk(), new Amok()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Amok"), null, monk.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(player1, "Amok"), null, monk.getId());
        harness.passBothPriorities();

        assertThat(monk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, cardName));
    }
}
