package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalliationAccord.class, AzoriusFirstWing.class, CacklingFlames.class})
class PalliationAccordTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature becoming tapped puts a palliation counter on Palliation Accord")
    void opponentCreatureTapAddsCounter() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());

        tap(creature);
        resolveAllTriggers();

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent creature becoming tapped adds its own palliation counter")
    void eachOpponentCreatureTapAddsCounter() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());

        tap(firstCreature);
        tap(secondCreature);
        resolveAllTriggers();

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only opponent creatures trigger Palliation Accord")
    void ignoresOwnCreaturesAndOpponentNoncreatures() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AzoriusFirstWing());

        tap(ownCreature);
        assertThat(gd.stack).isEmpty();

        Permanent opponentAccord = harness.addToBattlefieldAndReturn(player2, new PalliationAccord());
        tap(opponentAccord);
        assertThat(gd.stack).isEmpty();
        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isZero();
    }

    @Test
    @DisplayName("Removing a palliation counter prevents the next damage to its controller")
    void removesCounterAndPreventsNextDamage() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isZero();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The prevention shield applies only to Palliation Accord's controller")
    void preventsDamageOnlyToController() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a palliation counter")
    void cannotActivateWithoutCounter() {
        harness.addToBattlefield(player1, new PalliationAccord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declaring an opponent's creature as an attacker adds a palliation counter")
    void attackingOpponentCreatureAddsCounter() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        addCreatureReady(player2, new AzoriusFirstWing());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations pay counters immediately and combine their prevention")
    void multipleActivationsCombinePrevention() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isEqualTo(1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isZero();
        resolveAllTriggers();

        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The activated ability still prevents damage if Palliation Accord leaves before resolution")
    void preventionResolvesAfterSourceLeaves() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(accord);
        gd.playerGraveyards.get(player1.getId()).add(accord.getCard());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
    @Test
    @DisplayName("Unused prevention expires at the end of the turn")
    void unusedPreventionExpiresAtEndOfTurn() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 1);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }
    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
