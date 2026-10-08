package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeshotElder.class, CopperMyr.class, CarapaceForger.class, GalvanicBlast.class, KothOfTheHammer.class})
class SpikeshotElderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player with base power 1")
    void deals1DamageToPlayerWithBasePower() {
        addReadyElder(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage equal to boosted power to target player")
    void dealsBoostedDamageToPlayer() {
        Permanent elder = addReadyElder(player1);
        elder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // power becomes 1+2 = 3
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Copper Myr");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Copper Myr");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 survives")
    void deals1DamageDoesNotKill2Toughness() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Deals boosted damage equal to power, killing a 2/2")
    void dealsBoostedDamageToCreature() {
        Permanent elder = addReadyElder(player1);
        elder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 1+1 = 2
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Does not tap when activating ability")
    void doesNotTapOnActivation() {
        Permanent elder = addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(elder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times with enough mana")
    void canActivateMultipleTimes() {
        addReadyElder(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedOnActivation() {
        addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals no damage when power is 0")
    void dealsNoDamageWhenPowerIsZero() {
        Permanent elder = addReadyElder(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        elder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.activateAbility(player1, 0, null, targetId);

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Uses last-known power if Spikeshot Elder is removed before resolution")
    void usesLastKnownPowerIfSourceRemoved() {
        addReadyElder(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Spikeshot Elder"));
        harness.assertInGraveyard(player1, "Spikeshot Elder");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void usesChangedPowerAsSourceLastExistedOnBattlefield() {
        Permanent elder = addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, player2.getId());
        elder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, elder.getId());
        harness.assertInGraveyard(player1, "Spikeshot Elder");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void usesPowerAtResolutionRatherThanActivation() {
        Permanent elder = addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, player2.getId());
        elder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SpikeshotElder());
        elder.setSummoningSick(true);
        elder.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(elder.isTapped()).isTrue();
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addReadyElder(player1);
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void requiresTwoRedMana() {
        addReadyElder(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyElder(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SpikeshotElder());
        perm.setSummoningSick(false);
        return perm;
    }
}
