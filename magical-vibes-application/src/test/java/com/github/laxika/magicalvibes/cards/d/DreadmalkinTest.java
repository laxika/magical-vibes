package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.t.TheWanderer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dreadmalkin.class, TheWanderer.class, AjanisPridemate.class})
class DreadmalkinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts two +1/+1 counters on Dreadmalkin")
    void sacrificesAnotherCreatureAndGetsCounters() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Ajani's Pridemate");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreadmalkin).doesNotContain(creature);
    }

    @Test
    @DisplayName("Sacrificing another planeswalker puts two +1/+1 counters on Dreadmalkin")
    void sacrificesAnotherPlaneswalkerAndGetsCounters() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new TheWanderer());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "The Wanderer");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreadmalkin).doesNotContain(wanderer);
    }

    @Test
    @DisplayName("The ability cannot sacrifice Dreadmalkin itself")
    void requiresAnotherPermanent() {
        addReadyDreadmalkin(player1);
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability requires two generic mana and one black mana")
    void requiresMana() {
        addReadyDreadmalkin(player1);
        harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately but counters wait for resolution")
    void sacrificeIsPaidBeforeResolution() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Ajani's Pridemate");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Dreadmalkin can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        dreadmalkin.setSummoningSick(true);
        dreadmalkin.setTapped(true);
        harness.addToBattlefield(player1, new AjanisPridemate());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(dreadmalkin.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ajani's Pridemate");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AjanisPridemate());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required black mana")
    void requiresBlackMana() {
        addReadyDreadmalkin(player1);
        harness.addToBattlefield(player1, new AjanisPridemate());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotInGraveyard(player1, "Ajani's Pridemate");
    }

    @Test
    @DisplayName("Menace prohibits a single blocker")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new Dreadmalkin());
        addCreatureReady(player2, new AjanisPridemate());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new Dreadmalkin());
        Permanent first = addCreatureReady(player2, new AjanisPridemate());
        Permanent second = addCreatureReady(player2, new AjanisPridemate());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent addReadyDreadmalkin(Player player) {
        Permanent dreadmalkin = addCreatureReady(player, new Dreadmalkin());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dreadmalkin;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
