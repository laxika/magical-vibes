package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmberSwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("When Ember Swallower becomes monstrous, each player sacrifices up to three lands")
    void becomingMonstrousSacrificesThreeLandsFromEachPlayer() {
        Permanent swallower = addReadySwallower();
        addLands(player1, Mountain::new, 3);
        addLands(player2, Forest::new, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(swallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(swallower.isMonstrous()).isTrue();
        assertThat(countLands(player1)).isZero();
        assertThat(countLands(player2)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each player chooses three lands when they control more than three")
    void eachPlayerChoosesThreeLandsWhenTheyControlMoreThanThree() {
        addReadySwallower();
        addLands(player1, Mountain::new, 4);
        addLands(player2, Forest::new, 4);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1,
                findPermanents(player1, "Mountain").stream().limit(3).map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player2,
                findPermanents(player2, "Forest").stream().limit(3).map(Permanent::getId).toList());

        assertThat(countLands(player1)).isEqualTo(1);
        assertThat(countLands(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ember Swallower's monstrosity ability can resolve only once")
    void monstrosityOnlyResolvesOnce() {
        addReadySwallower();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadySwallower() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new EmberSwallower());
        swallower.setSummoningSick(false);
        return swallower;
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, Supplier<Card> landSupplier, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, landSupplier.get());
        }
    }

    private long countLands(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
