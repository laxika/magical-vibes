package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hateflayer.class, BallynockTrapper.class})
class HateflayerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} and untapping deals 5 (its power) damage to target player")
    void dealsPowerDamageToPlayer() {
        Permanent hateflayer = addTapped(player1, new Hateflayer()); // 5/5
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Paying {Q} untapped the source.
        assertThat(hateflayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Wither: damage to a creature is dealt as -1/-1 counters, killing a 2/2")
    void witherDealsMinusCountersToCreature() {
        addTapped(player1, new Hateflayer()); // 5/5, wither
        Permanent target = addCreatureReady(player2, new BallynockTrapper()); // 2/2
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        UUID targetId = target.getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player2, "Ballynock Trapper");
    }

    @Test
    @DisplayName("Cannot activate while untapped ({Q} requires the source to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new Hateflayer());
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapping is paid immediately, before the damage resolves")
    void untapsAsActivationCost() {
        Permanent source = addTapped(player1, new Hateflayer());
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(source.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness cannot pay the untap cost")
    void summoningSicknessPreventsActivation() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Hateflayer());
        source.tap();
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage uses the source's power at resolution")
    void usesCurrentPowerAtResolution() {
        Permanent source = addTapped(player1, new Hateflayer());
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);
        harness.activateAbility(player1, 0, null, player2.getId());

        source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An unpaid mana cost does not untap the source")
    void insufficientManaPreventsActivation() {
        Permanent source = addTapped(player1, new Hateflayer());
        harness.addMana(player1, ManaColor.RED, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
