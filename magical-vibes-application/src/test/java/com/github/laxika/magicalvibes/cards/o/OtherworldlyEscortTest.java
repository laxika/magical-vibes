package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtherworldlyEscort.class, GrizzlyBears.class})
class OtherworldlyEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from death with charge counters as a Spirit Detective, but not after dying as a Spirit")
    void returnsAsSpiritDetectiveOnce() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());

        kill(escort);

        Permanent returned = findPermanent(player1, "Otherworldly Escort");
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.DETECTIVE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HUMAN)).isFalse();

        kill(returned);

        harness.assertNotOnBattlefield(player1, "Otherworldly Escort");
        harness.assertInGraveyard(player1, "Otherworldly Escort");
    }

    @Test
    @DisplayName("Destroys a creature that dealt damage to you after paying mana and a charge counter")
    void destroysCreatureThatDealtDamageToYou() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());
        escort.setCounterCount(CounterType.CHARGE, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(attacker.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(escort.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(escort.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that did not deal damage to you this turn")
    void cannotTargetUndamagingCreature() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());
        escort.setCounterCount(CounterType.CHARGE, 1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns under its owner's control after dying while stolen")
    void returnsToOwnerAfterDyingWhileStolen() {
        Permanent escort = addCreatureReady(player2, new OtherworldlyEscort());
        gd.stolenCreatures.put(escort.getId(), player1.getId());

        kill(escort);

        harness.assertNotOnBattlefield(player2, "Otherworldly Escort");
        Permanent returned = findPermanent(player1, "Otherworldly Escort");
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.SPIRIT)).isTrue();
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can destroy a creature that dealt noncombat damage to you")
    void destroysCreatureThatDealtNoncombatDamage() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());
        escort.setCounterCount(CounterType.CHARGE, 1);
        Permanent source = addCreatureReady(player2, new OtherworldlyEscort());
        gd.noncombatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Otherworldly Escort").getId()).isNotEqualTo(source.getId());
        assertThat(escort.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(escort.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a charge counter")
    void cannotActivateWithoutChargeCounter() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());
        Permanent source = addCreatureReady(player2, new OtherworldlyEscort());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(escort.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that damaged only another player")
    void cannotTargetCreatureThatDamagedAnotherPlayer() {
        Permanent escort = addCreatureReady(player1, new OtherworldlyEscort());
        escort.setCounterCount(CounterType.CHARGE, 1);
        Permanent source = addCreatureReady(player2, new OtherworldlyEscort());
        gd.noncombatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's upkeep")
    void canBeCastDuringOpponentsUpkeep() {
        advanceToUpkeep(player2);

        harness.castFromHand(player1, new OtherworldlyEscort(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Otherworldly Escort");
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
