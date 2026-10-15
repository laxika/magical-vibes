package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AzusasManyJourneys;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.l.LikenessOfTheSeeker;
import com.github.laxika.magicalvibes.cards.n.NyxWeaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Storyweave.class, GrizzlyBears.class, NyxWeaver.class,
        AzusasManyJourneys.class, LikenessOfTheSeeker.class, BearerOfMemory.class})
class StoryweaveTest extends BaseCardTest {

    @Test
    @DisplayName("The creature mode puts two +1/+1 counters on a creature you control")
    void creatureModePutsCountersOnControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Saga mode puts lore counters on a Saga and prepares the next qualifying entry")
    void sagaModePutsLoreCountersAndPreparesEntry() {
        Permanent saga = addSagaTarget();
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 1, saga.getId());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        Permanent creature = castNyxWeaver(player1);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The delayed replacement ignores non-enchantment creatures and is consumed once")
    void delayedReplacementOnlyAppliesToNextEnchantmentCreatureEntry() {
        Permanent saga = addSagaTarget();
        castStoryweaveSagaMode(saga);

        Permanent bears = castCreature(player1, new GrizzlyBears());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent firstWeaver = castNyxWeaver(player1);
        Permanent secondWeaver = castNyxWeaver(player1);

        assertThat(firstWeaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondWeaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Storyweave modes enforce their target restrictions")
    void modesEnforceTargetRestrictions() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureModeAddsToExistingCountersWithoutPreparingAnEntryBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        Permanent next = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());
        assertThat(next.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sagaModeRejectsAnOpponentsSaga() {
        Permanent saga = harness.addToBattlefieldAndReturn(player2, new AzusasManyJourneys());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, saga.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerCanChooseTheOrderOfSimultaneouslyTriggeredChapters() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AzusasManyJourneys());
        saga.setCounterCount(CounterType.LORE, 1);

        castStoryweaveSagaMode(saga);

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must be offered a choice of chapter II and III's stack order")
                .isTrue();
    }

    @Test
    void sagaModeTriggersBothCrossedChaptersAndBoostsTheReturningBackFace() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AzusasManyJourneys());
        saga.setCounterCount(CounterType.LORE, 1);

        castStoryweaveSagaMode(saga);

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Azusa's Many Journeys");
        Permanent seeker = findPermanent(player1, "Likeness of the Seeker");
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent next = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());
        assertThat(next.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void anOpponentsEnchantmentCreatureDoesNotConsumeTheEntryBonus() {
        prepareEntryBonusWithRealSaga();

        Permanent opposing = harness.enterBattlefieldAndReturn(player2, new BearerOfMemory());
        Permanent own = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void allEnchantmentCreaturesInTheNextSimultaneousEntryGetCounters() {
        prepareEntryBonusWithRealSaga();
        Permanent first = new Permanent(new BearerOfMemory());
        Permanent second = new Permanent(new BearerOfMemory());
        List<Permanent> entered = new ArrayList<>();

        harness.inMutationScope(() -> {
            harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                    gd, player1.getId(), first, Set.of(), entered);
            entered.add(first);
            harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                    gd, player1.getId(), second, Set.of(), entered);
        });

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent later = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());
        assertThat(later.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void unusedEntryBonusExpiresAtEndOfTurn() {
        prepareEntryBonusWithRealSaga();
        harness.passUntil(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.UPKEEP);

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void removingTheSagaTargetPreventsTheEntryBonus() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AzusasManyJourneys());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 1, saga.getId());
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerHands.get(player1.getId()).add(saga.getOriginalCard());

        harness.passBothPriorities();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Storyweave");
    }

    @Test
    void multipleResolvedStoryweavesAddTheirEntryBonuses() {
        prepareEntryBonusWithRealSaga();
        prepareEntryBonusWithRealSaga();

        Permanent first = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new BearerOfMemory());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareEntryBonusWithRealSaga() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AzusasManyJourneys());
        castStoryweaveSagaMode(saga);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castStoryweaveSagaMode(Permanent saga) {
        harness.setHand(player1, List.of(new Storyweave()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 1, saga.getId());
        harness.passBothPriorities();
    }

    private Permanent addSagaTarget() {
        Card sagaCard = new Card();
        sagaCard.setName("Test Saga");
        sagaCard.setType(CardType.ENCHANTMENT);
        sagaCard.setSubtypes(List.of(CardSubtype.SAGA));
        return harness.addToBattlefieldAndReturn(player1, sagaCard);
    }

    private Permanent castNyxWeaver(com.github.laxika.magicalvibes.model.Player player) {
        return castCreature(player, new NyxWeaver());
    }

    private Permanent castCreature(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        harness.castFromHand(player, card, card instanceof NyxWeaver ? "{1}{B}{G}" : "{1}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
