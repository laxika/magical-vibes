package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AnimusOfNightsReach;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.v.VoltageSurge;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLongReachOfNight.class, AnimusOfNightsReach.class, Forest.class, BearerOfMemory.class, VoltageSurge.class})
class TheLongReachOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II let each opponent discard instead of sacrificing a creature")
    void opponentMayDiscardInsteadOfSacrificingCreature() {
        harness.addToBattlefield(player2, new BearerOfMemory());
        harness.setHand(player2, List.of(new VoltageSurge()));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Bearer of Memory");
        harness.assertInGraveyard(player2, "Voltage Surge");
    }

    @Test
    @DisplayName("The sacrifice option only offers creatures")
    void opponentCanSacrificeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new BearerOfMemory());
        harness.setHand(player2, List.of());
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId(),
                gd.playerBattlefields.get(player2.getId()).get(2).getId());

        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertInGraveyard(player2, "Bearer of Memory");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Chapter III transforms the Saga under its controller's control")
    void chapterIIITransformsIntoAnimus() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent animus = findPermanent(player1, "Animus of Night's Reach");
        assertThat(animus).isNotNull();
        assertThat(animus.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Animus gets +X/+0 for creature cards in the defending player's graveyard")
    void animusBoostsForDefendingPlayersCreatureCards() {
        Permanent animus = addCreatureReady(player1, new AnimusOfNightsReach());
        harness.setGraveyard(player2, List.of(new BearerOfMemory(), new BearerOfMemory(), new VoltageSurge()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, animus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, animus)).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An opponent with no creatures may decline to discard for either chapter")
    void opponentWithoutCreaturesMayDeclineDiscard(int initialLore) {
        harness.setHand(player2, List.of(new Forest()));
        addSagaWithLore(initialLore);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);

        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An opponent with neither creatures nor cards does nothing")
    void opponentWithNoResourcesDoesNothing(int initialLore) {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());
        addSagaWithLore(initialLore);

        advanceToNextChapter();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter I triggers when the Saga is cast and leaves its controller's resources alone")
    void castingSagaTriggersFirstChapterForOpponentOnly() {
        harness.addToBattlefield(player1, new BearerOfMemory());
        harness.addToBattlefield(player2, new BearerOfMemory());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new TheLongReachOfNight(), "{3}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Long Reach of Night");
        harness.assertOnBattlefield(player1, "Bearer of Memory");
        harness.assertNotOnBattlefield(player2, "Bearer of Memory");
        harness.assertInGraveyard(player2, "Bearer of Memory");
    }

    @Test
    @DisplayName("Chapter III creates a new summoning-sick permanent without lore counters")
    void transformationCreatesNewPermanent() {
        Permanent saga = addSagaWithLore(2);
        saga.tap();

        advanceToNextChapter();

        Permanent animus = findPermanent(player1, "Animus of Night's Reach");
        assertThat(animus.getId()).isNotEqualTo(saga.getId());
        assertThat(animus.isTapped()).isFalse();
        assertThat(animus.isSummoningSick()).isTrue();
        assertThat(animus.getCounterCount(CounterType.LORE)).isZero();
        harness.assertNotOnBattlefield(player1, "The Long Reach of Night");
        harness.assertNotInGraveyard(player1, "The Long Reach of Night");
    }

    @Test
    @DisplayName("Animus ignores its controller's graveyard and noncreature cards")
    void animusDoesNotCountOtherGraveyardsOrNoncreatures() {
        Permanent animus = addCreatureReady(player1, new AnimusOfNightsReach());
        harness.setGraveyard(player1, List.of(new BearerOfMemory(), new BearerOfMemory()));
        harness.setGraveyard(player2, List.of(new Forest(), new VoltageSurge()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, animus)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, animus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animus counts creatures at resolution and keeps that bonus until cleanup")
    void animusCountsAtResolutionAndBonusExpires() {
        Permanent animus = addCreatureReady(player1, new AnimusOfNightsReach());
        harness.setGraveyard(player2, List.of(new BearerOfMemory()));
        harness.addToBattlefield(player2, new BearerOfMemory());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player2, List.of(new BearerOfMemory(), new BearerOfMemory(), new Forest()));

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, animus)).isEqualTo(2);
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, animus)).isEqualTo(2);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, animus)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, animus)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, animus)).isEqualTo(4);
    }
    @Test
    @DisplayName("Animus cannot be blocked by only one creature")
    void animusRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new AnimusOfNightsReach());
        addCreatureReady(player2, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Animus")
    void animusCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new AnimusOfNightsReach());
        Permanent first = addCreatureReady(player2, new BearerOfMemory());
        Permanent second = addCreatureReady(player2, new BearerOfMemory());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLongReachOfNight());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
    }
}
