package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Suncrusher.class, Arachnoid.class, WayfarersBauble.class})
class SuncrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on Suncrusher for each color spent")
    void sunburstCountsDistinctColorsSpent() {
        harness.setHand(player1, List.of(new Suncrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent suncrusher = findPermanent(player1, "Suncrusher");
        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sunburst counts a repeated color only once")
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new Suncrusher()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent suncrusher = findPermanent(player1, "Suncrusher");
        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sunburst ignores colorless mana")
    void sunburstIgnoresColorlessMana() {
        harness.setHand(player1, List.of(new Suncrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent suncrusher = findPermanent(player1, "Suncrusher");
        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Suncrusher destroys a target creature after removing a +1/+1 counter")
    void destroysTargetCreature() {
        Permanent suncrusher = addReadySuncrusher(player1, 1);
        Permanent arachnoid = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareTurn();

        harness.activateAbility(player1, 0, null, arachnoid.getId());
        harness.passBothPriorities();

        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(suncrusher.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Suncrusher's destroy ability requires a +1/+1 counter")
    void destroyAbilityRequiresCounter() {
        Permanent suncrusher = addReadySuncrusher(player1, 0);
        Permanent arachnoid = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, arachnoid.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(suncrusher.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Suncrusher cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadySuncrusher(player1, 1);
        Permanent bauble = harness.addToBattlefieldAndReturn(player2, new WayfarersBauble());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bauble.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Suncrusher can remove a +1/+1 counter to return itself to its owner's hand")
    void returnsItselfToHand() {
        Permanent suncrusher = addReadySuncrusher(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(suncrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(suncrusher.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Suncrusher");
        harness.assertInHand(player1, "Suncrusher");
    }

    @Test
    @DisplayName("Suncrusher returns to its owner's hand when controlled by another player")
    void returnsItselfToOwnersHandWhenControlledByAnotherPlayer() {
        Suncrusher card = new Suncrusher();
        card.setOwnerId(player2.getId());
        Permanent suncrusher = addCreatureReady(player1, card);
        suncrusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Suncrusher");
        harness.assertInHand(player2, "Suncrusher");
    }

    private Permanent addReadySuncrusher(Player player, int counters) {
        Permanent suncrusher = addCreatureReady(player, new Suncrusher());
        suncrusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return suncrusher;
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
