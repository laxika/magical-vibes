package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DuchessWaywardTavernkeep.class, GrizzlyBears.class})
class DuchessWaywardTavernkeepTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a quest counter on each creature that deals combat damage to a player")
    void putsQuestCounterOnCombatDamageDealer() {
        Permanent duchess = addCreatureReady(player1, new DuchessWaywardTavernkeep());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(duchess.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removes a quest counter from a controlled permanent to create a Junk")
    void removesQuestCounterToCreateJunk() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duchess.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a quest counter on a controlled permanent")
    void requiresControlledQuestCounter() {
        harness.addToBattlefield(player1, new DuchessWaywardTavernkeep());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    void eachCombatDamageDealerIncludingDuchessGetsOneCounter() {
        Permanent duchess = addCreatureReady(player1, new DuchessWaywardTavernkeep());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(duchess.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void opposingCombatDamageDoesNotAddQuestCounters() {
        Permanent duchess = addCreatureReady(player1, new DuchessWaywardTavernkeep());
        duchess.tap();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(duchess.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void cannotPayWithAnotherCounterType() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(duchess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Junk")).isEmpty();
    }

    @Test
    void cannotPayWithOpponentsQuestCounter() {
        harness.addToBattlefield(player1, new DuchessWaywardTavernkeep());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DuchessWaywardTavernkeep());
        opponent.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(opponent.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void canRemoveQuestCounterFromNoncreaturePermanentDuringCombat() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        Permanent junk = findPermanent(player1, "Junk");
        junk.setCounterCount(CounterType.QUEST, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(junk.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).hasSize(2);
    }

    @Test
    void junkSacrificesToExileTopCardAndAllowsCastingWithNormalManaCost() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        DuchessWaywardTavernkeep topCard = new DuchessWaywardTavernkeep();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        assertThat(findPermanents(player1, "Junk")).isEmpty();
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void junkCannotBeActivatedDuringCombat() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }
}
