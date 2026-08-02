package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceMemoryAdept;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulOfShandalarTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability burns the target player and a creature they control")
    void battlefieldAbilityHitsPlayerAndTheirCreature() {
        addReadySoul();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("The creature target is optional")
    void creatureTargetIsOptional() {
        addReadySoul();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A targeted planeswalker takes the damage and its controller's creature is hit too")
    void hitsPlaneswalkerAndItsControllersCreature() {
        addReadySoul();
        Permanent jace = new Permanent(new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(jace);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(jace.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("A creature the targeted player does not control is an illegal second target")
    void rejectsCreatureOfAnotherController() {
        addReadySoul();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability exiles the card and deals the same damage")
    void graveyardAbilityExilesAndBurns() {
        Card soul = new SoulOfShandalar();
        harness.setGraveyard(player1, List.of(soul));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbilityWithTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(soul.getId()));
    }

    @Test
    @DisplayName("Graveyard ability rejects an illegal creature target before paying any cost")
    void graveyardAbilityRejectsIllegalCreatureTarget() {
        Card soul = new SoulOfShandalar();
        harness.setGraveyard(player1, List.of(soul));
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul);
    }

    private void addReadySoul() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfShandalar());
        soul.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
