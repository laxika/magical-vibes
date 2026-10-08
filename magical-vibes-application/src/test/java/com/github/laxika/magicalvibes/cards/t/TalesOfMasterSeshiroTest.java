package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.cards.s.SeshirosLivingLegacy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalesOfMasterSeshiro.class, SeshirosLivingLegacy.class, BearerOfMemory.class, MobilizerMech.class})
class TalesOfMasterSeshiroTest extends BaseCardTest {

    @Test
    void chapterIPlacesCounterAndGrantsVigilanceToCreatureYouControl() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        Permanent opposingCreature = addCreatureReady(player2, new BearerOfMemory());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(creature.getId()).doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIITargetsVehicleYouControl() {
        addSagaWithLore(1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new MobilizerMech());
        Permanent opposingVehicle = harness.addToBattlefieldAndReturn(player2, new MobilizerMech());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(vehicle.getId()).doesNotContain(opposingVehicle.getId());
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void chapterIIITransformsIntoSeshirosLivingLegacy() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, "Seshiro's Living Legacy");
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void enteringSagaTriggersChapterIImmediately() {
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());

        harness.castFromHand(player1, new TalesOfMasterSeshiro(), "{4}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(findPermanent(player1, "Tales of Master Seshiro")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void chaptersRequireTargetWhenCreatureIsAvailable(int initialLore) {
        addSagaWithLore(initialLore);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void vigilanceExpiresButCounterRemains() {
        addSagaWithLore(0);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chapterDoesNothingWhenTargetChangesControllerBeforeResolution() {
        addSagaWithLore(1);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void chapterStillResolvesWhenSagaLeavesBattlefield() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void finalChapterReturnsNewPermanentThatCanAttackWithoutTapping() {
        Permanent saga = addSagaWithLore(2);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        saga.tap();
        advanceToNextChapter();
        resolveAllTriggers();

        Permanent legacy = findPermanent(player1, "Seshiro's Living Legacy");
        assertThat(legacy.getId()).isNotEqualTo(saga.getId());
        assertThat(legacy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(legacy.getCounterCount(CounterType.LORE)).isZero();
        assertThat(legacy.isTapped()).isFalse();
        assertThat(gd.findExiledCard(saga.getOriginalCard().getId())).isNull();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(legacy.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void finalChapterDoesNotReturnSagaThatAlreadyLeftBattlefield() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getOriginalCard());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TalesOfMasterSeshiro());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
