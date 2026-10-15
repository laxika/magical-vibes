package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.e.ErrantDoomsayers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronclawBuzzardiers.class, AshcoatBear.class, DurkwoodBaloth.class, ErrantDoomsayers.class})
class IronclawBuzzardiersTest extends BaseCardTest {

    @Test
    @DisplayName("Can block an attacker with power 1")
    void canBlockPowerOne() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addCreatureReady(player1, new ErrantDoomsayers());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot block an attacker with power 2")
    void cannotBlockPowerTwo() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too high");
    }

    @Test
    @DisplayName("Cannot block an attacker with power greater than 2")
    void cannotBlockPowerGreaterThanTwo() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too high");
    }

    @Test
    @DisplayName("Red activation grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent buzzardiers = addReadyBuzzardiers(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Requires red mana to activate the flying ability")
    void requiresRedManaToActivateFlyingAbility() {
        Permanent buzzardiers = addReadyBuzzardiers(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, buzzardiers), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Blocking restriction uses the attacker's current power")
    void cannotBlockPowerOneCreatureWithPlusOneCounter() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addCreatureReady(player1, new ErrantDoomsayers());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too high");
    }

    @Test
    @DisplayName("A tapped summoning-sick source grants flying only to itself on resolution")
    void flyingAbilityNeedsNeitherTapNorHasteAndOnlyAffectsSource() {
        Permanent buzzardiers = addReadyBuzzardiers(player1);
        buzzardiers.setSummoningSick(true);
        buzzardiers.tap();
        Permanent otherBuzzardiers = addReadyBuzzardiers(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, buzzardiers), null, null);

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBuzzardiers, Keyword.FLYING)).isFalse();
        assertThat(buzzardiers.isTapped()).isTrue();
    }

    private Permanent addReadyBuzzardiers(Player player) {
        return addCreatureReady(player, new IronclawBuzzardiers());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
