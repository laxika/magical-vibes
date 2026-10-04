package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Carnassid;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.cards.s.SpikeFeeder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carnassid.class, Heartstone.class, HornetCannon.class, SliverQueen.class, SpikeFeeder.class})
class HeartstoneTest extends BaseCardTest {

    @Test
    @DisplayName("A creature's generic ability costs one mana and resolves normally")
    void reducesGenericCostToOneMana() {
        harness.addToBattlefield(player1, new SliverQueen());
        harness.addToBattlefield(player1, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Sliver");
    }

    @Test
    @DisplayName("Multiple Heartstones still require one mana for a generic ability")
    void multipleHeartstonesPreserveOneManaFloor() {
        harness.addToBattlefield(player1, new SliverQueen());
        harness.addToBattlefield(player1, new Heartstone());
        harness.addToBattlefield(player2, new Heartstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sliver");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Heartstones cannot replace colored mana with generic mana")
    void doesNotReduceColoredManaRequirements() {
        harness.addToBattlefield(player1, new Carnassid());
        harness.addToBattlefield(player1, new Heartstone());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("An ability with no mana cost remains free and still pays its counter cost")
    void doesNotAddManaToFreeAbilities() {
        var feeder = harness.enterBattlefieldAndReturn(player1, new SpikeFeeder());
        harness.addToBattlefield(player2, new Heartstone());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reduces a creature's activated ability by one generic mana for any player")
    void reducesCreatureAbilityForAnyPlayer() {
        harness.addToBattlefield(player1, new Carnassid());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a creature ability below one mana")
    void doesNotReduceCreatureAbilityBelowOneMana() {
        harness.addToBattlefield(player1, new SliverQueen());
        harness.addToBattlefield(player2, new Heartstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce activated abilities of noncreatures")
    void doesNotReduceNoncreatureAbility() {
        harness.addToBattlefield(player1, new HornetCannon());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
