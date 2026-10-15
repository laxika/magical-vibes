package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GargoyleCastle;
import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ObsessivePursuit.class, GargoyleCastle.class, RuneclawBear.class, ErdwalIlluminator.class})
class ObsessivePursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by making its controller lose life and creating a Clue")
    void entersWithLifeLossAndClue() {
        harness.setHand(player1, List.of(new ObsessivePursuit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers on upkeep with another life loss and Clue")
    void triggersOnUpkeep() {
        harness.addToBattlefield(player1, new ObsessivePursuit());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Puts counters equal to permanents sacrificed and grants lifelink at three")
    void scalesWithSacrificedPermanents() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent firstCastle = addCastle();
        addCastle();
        addCastle();
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 15);

        sacrificeCastle(firstCastle);
        sacrificeCastle(findPermanents(player1, "Gargoyle Castle").getFirst());
        sacrificeCastle(findPermanents(player1, "Gargoyle Castle").getFirst());
        resolveAllTriggers();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void entryCreatesClueWithoutInvestigating() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new ObsessivePursuit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void upkeepCreatesClueWithoutInvestigating() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.addToBattlefield(player1, new ObsessivePursuit());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ObsessivePursuit());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void zeroSacrificesStillTriggersButDoesNotGrantCountersOrLifelink() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void cannotTargetNonattackingCreature() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent nonattacker = addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenOpponentAttacks() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent attacker = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void twoSacrificesGiveCountersWithoutLifelinkAndOnlyOneAttackTrigger() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent firstCastle = addCastle();
        Permanent secondCastle = addCastle();
        Permanent firstAttacker = addCreatureReady(player1, new RuneclawBear());
        Permanent secondAttacker = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        sacrificeCastle(firstCastle);
        sacrificeCastle(secondCastle);
        resolveAllTriggers();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        harness.handlePermanentChosen(player1, firstAttacker.getId());
        resolveAllTriggers();

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.LIFELINK)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsSacrificesInResponseAndLifelinkExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent firstCastle = addCastle();
        Permanent secondCastle = addCastle();
        Permanent thirdCastle = addCastle();
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 15);
        sacrificeCastle(firstCastle);
        sacrificeCastle(secondCastle);
        resolveAllTriggers();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        sacrificeCastle(thirdCastle);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentsSacrificesDoNotCount() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new GargoyleCastle());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Gargoyle Castle");

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void clueCanBeSacrificedToDrawAndCountsForAttackTrigger() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue),
                0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Clue");
        harness.assertInHand(player1, "Runeclaw Bear");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addCastle() {
        return addCreatureReady(player1, new GargoyleCastle());
    }

    private void sacrificeCastle(Permanent castle) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(castle), 1, null, null);
    }
}
