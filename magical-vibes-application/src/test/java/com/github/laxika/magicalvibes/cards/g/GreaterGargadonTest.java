package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreaterGargadon.class, AshcoatBear.class, Forest.class, PrismaticLens.class})
class GreaterGargadonTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Greater Gargadon with ten time counters")
    void suspendExilesWithTenTimeCounters() {
        GreaterGargadon card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);
    }

    @Test
    @DisplayName("Suspend can only be activated at sorcery speed")
    void suspendRequiresSorcerySpeed() {
        GreaterGargadon card = new GreaterGargadon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The suspended ability lets its controller sacrifice a creature or land")
    void sacrificesChosenPermanentToRemoveTimeCounter() {
        GreaterGargadon card = suspendCard();
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateExiledAbility(player1, card.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bear.getId(), forest.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear).doesNotContain(forest);
    }

    @Test
    @DisplayName("The suspended ability also accepts a noncreature artifact")
    void sacrificesArtifactToRemoveTimeCounter() {
        GreaterGargadon card = suspendCard();
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateExiledAbility(player1, card.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(lens.getId(), forest.getId());

        harness.handlePermanentChosen(player1, lens.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest).doesNotContain(lens);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentForest);
    }

    @Test
    @DisplayName("Removing the last time counter offers the suspended card for free")
    void removingLastTimeCounterOffersFreeCast() {
        GreaterGargadon card = suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        harness.activateExiledAbility(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("The suspended ability cannot be activated while Greater Gargadon is on the battlefield")
    void abilityRequiresSuspendedSource() {
        harness.addToBattlefield(player1, new GreaterGargadon());
        addCreatureReady(player1, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Suspending is a special action that does not use the stack")
    void suspendingDoesNotUseStack() {
        suspendCard();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend removes a counter at its owner's upkeep, but not its opponent's upkeep")
    void upkeepRemovesCounterOnlyForOwner() {
        GreaterGargadon card = suspendCard();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, while counter removal uses the stack at instant speed")
    void sacrificeOnOpponentsTurnPaysCostBeforeResolution() {
        GreaterGargadon card = suspendCard();
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateExiledAbility(player1, card.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
    }

    @Test
    @DisplayName("The suspended ability cannot be activated without a permanent to sacrifice")
    void cannotActivateWithoutSacrifice() {
        GreaterGargadon card = suspendCard();

        assertThatThrownBy(() -> harness.activateExiledAbility(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast leaves Gargadon exiled and unable to activate its sacrifice ability")
    void decliningCastLeavesSourceNoLongerSuspended() {
        GreaterGargadon card = suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.activateExiledAbility(player1, card.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThatThrownBy(() -> harness.activateExiledAbility(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The suspended creature can attack immediately after the free cast")
    void suspendedCreatureHasHaste() {
        GreaterGargadon card = suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.activateExiledAbility(player1, card.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent gargadon = findPermanent(player1, "Greater Gargadon");
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gargadon.isAttacking()).isTrue();
    }

    private GreaterGargadon suspendCard() {
        GreaterGargadon card = new GreaterGargadon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
