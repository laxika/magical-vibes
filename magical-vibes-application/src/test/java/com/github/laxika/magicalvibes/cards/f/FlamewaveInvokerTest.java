package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamewaveInvoker.class, FugitiveWizard.class, JaceBeleren.class})
class FlamewaveInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Flamewave Invoker");
    }

    @Test
    @DisplayName("Activating ability does not tap Flamewave Invoker")
    void activatingDoesNotTap() {
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 10);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving ability deals 5 damage to target player")
    void resolvingDealsFiveDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can target a planeswalker with the ability")
    void canTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target self with the ability")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Can activate multiple times to deal more damage")
    void canActivateMultipleTimes() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 16);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Resolving ability logs the damage")
    void resolvingLogsDamage() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("takes") && log.contains("5") && log.contains("damage"));
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a planeswalker or player");
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new FlamewaveInvoker());
        invoker.setSummoningSick(true);
        invoker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(invoker.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot pay the red mana requirement with colorless mana")
    void cannotActivateWithoutRedMana() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(invoker);
        gd.playerGraveyards.get(player1.getId()).add(invoker.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not damage a player when its planeswalker target leaves")
    void doesNotRedirectDamageFromMissingPlaneswalker() {
        addReadyInvoker(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.addMana(player1, ManaColor.RED, 8);
        harness.activateAbility(player1, 0, null, planeswalker.getId());

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyInvoker(Player player) {
        return addCreatureReady(player, new FlamewaveInvoker());
    }
}

