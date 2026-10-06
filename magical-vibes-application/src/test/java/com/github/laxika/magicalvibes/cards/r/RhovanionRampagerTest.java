package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhovanionRampager.class, AirElemental.class, Shock.class})
class RhovanionRampagerTest extends BaseCardTest {

    @Test
    void attackSacrificeAddsCountersEqualToSacrificedPower() {
        Permanent rampager = addCreatureReady(player1, new RhovanionRampager());
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(rampager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
    }

    @Test
    void choosingBetweenAttackSacrificesUsesChosenCreaturePower() {
        Permanent rampager = addCreatureReady(player1, new RhovanionRampager());
        Permanent first = addCreatureReady(player1, new AirElemental());
        Permanent second = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(rampager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
    }

    @Test
    void decliningAttackSacrificeDoesNothing() {
        Permanent rampager = addCreatureReady(player1, new RhovanionRampager());
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rampager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    void deathAmassesGoblinsEqualToLastKnownPower() {
        Permanent rampager = harness.addToBattlefieldAndReturn(player1, new RhovanionRampager());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, rampager.getId());
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsCreature() {
        Permanent rampager = addCreatureReady(player1, new RhovanionRampager());
        Permanent opponent = addCreatureReady(player2, new RhovanionRampager());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(rampager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rampager);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
    }

    @Test
    void sacrificingAnotherRampagerUsesItsModifiedPowerForBothAbilities() {
        Permanent attacker = addCreatureReady(player1, new RhovanionRampager());
        Permanent sacrifice = addCreatureReady(player1, new RhovanionRampager());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void subsequentDeathAddsCountersToExistingArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RhovanionRampager());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RhovanionRampager());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, first.getId());
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Goblin Army");

        harness.castAndResolveInstant(player2, 0, second.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Goblin Army")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }
}
