package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.g.GoShintaiOfBoundlessVigor;
import com.github.laxika.magicalvibes.cards.i.InvokeTheWinds;
import com.github.laxika.magicalvibes.cards.r.ReflectionOfKikiJiki;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FableOfTheMirrorBreaker.class, ReflectionOfKikiJiki.class, BearerOfMemory.class,
        GoShintaiOfBoundlessVigor.class, InvokeTheWinds.class, Confiscate.class})
class FableOfTheMirrorBreakerTest extends BaseCardTest {

    @Test
    void chapterICreatesGoblinShamanWhoseAttackCreatesTreasure() {
        addSaga(0);
        triggerChapter();
        harness.passBothPriorities();

        Permanent goblin = findPermanents(player1, "Goblin Shaman").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        goblin.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(goblin)));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void chapterIIDiscardsUpToTwoThenDrawsThatMany() {
        harness.setHand(player1, List.of(new BearerOfMemory(), new BearerOfMemory()));
        harness.setLibrary(player1, List.of(new BearerOfMemory(), new BearerOfMemory()));
        addSaga(1);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIIITransformsIntoReflectionUnderItsController() {
        addSaga(2);
        triggerChapter();
        harness.passBothPriorities();

        Permanent reflection = findPermanent(player1, "Reflection of Kiki-Jiki");
        assertThat(reflection).isNotNull();
        assertThat(reflection.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reflection);
    }

    @Test
    void reflectionCreatesHastyTokenCopySacrificedAtNextEndStep() {
        Permanent reflection = addReflection(player1);
        Permanent bears = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bearer of Memory")).hasSize(2);
        Permanent token = findPermanents(player1, "Bearer of Memory").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .containsExactly(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    void reflectionCannotTargetItselfOrAnOpponentCreature() {
        Permanent reflection = addReflection(player1);
        Permanent opponentBears = addCreatureReady(player2, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int reflectionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reflection);

        assertThatThrownBy(() -> harness.activateAbility(player1, reflectionIndex, 0, null, reflection.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonlegendary creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, reflectionIndex, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonlegendary creature you control");
    }

    @Test
    void castingSagaTriggersChapterIOnEntry() {
        harness.castFromHand(player1, new FableOfTheMirrorBreaker(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Shaman")).hasSize(1);
        assertThat(findPermanent(player1, "Fable of the Mirror-Breaker")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIICanDeclineDiscardingWithoutDrawing() {
        BearerOfMemory kept = new BearerOfMemory();
        BearerOfMemory libraryCard = new BearerOfMemory();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(libraryCard));
        addSaga(1);
        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIWithEmptyHandDoesNotDrawOrPrompt() {
        harness.setHand(player1, List.of());
        BearerOfMemory libraryCard = new BearerOfMemory();
        harness.setLibrary(player1, List.of(libraryCard));
        addSaga(1);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIICanDiscardOnlyOneCardAndDrawOne() {
        BearerOfMemory discarded = new BearerOfMemory();
        BearerOfMemory drawn = new BearerOfMemory();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addSaga(1);
        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void newlyReturnedReflectionCannotActivateItsTapAbility() {
        addSaga(2);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        triggerChapter();
        harness.passBothPriorities();
        Permanent reflection = findPermanent(player1, "Reflection of Kiki-Jiki");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void reflectionCannotTargetLegendaryCreature() {
        Permanent reflection = addReflection(player1);
        Permanent legendary = addCreatureReady(player1, new GoShintaiOfBoundlessVigor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, legendary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonlegendary creature you control");
    }

    @Test
    void copiedTokenIsSacrificedWhenEndStepTriggerResolves() {
        Permanent reflection = addReflection(player1);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, creature.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Bearer of Memory").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(token);
    }

    @Test
    void opponentControlledCopyCannotBeSacrificedByItsCreator() {
        Permanent reflection = addReflection(player1);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, creature.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Bearer of Memory").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new InvokeTheWinds()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.ensurePriority(player2);
        harness.castSorcery(player2, 0, token.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    void chapterIIIReturnsUnderTriggerControllersControlAfterSagaIsStolen() {
        Permanent saga = addSaga(2);
        triggerChapter();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Confiscate());
        aura.setAttachedTo(saga.getId());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Reflection of Kiki-Jiki")).hasSize(1);
        assertThat(findPermanents(player2, "Reflection of Kiki-Jiki")).isEmpty();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FableOfTheMirrorBreaker());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addReflection(Player player) {
        FableOfTheMirrorBreaker front = new FableOfTheMirrorBreaker();
        Permanent reflection = new Permanent(front);
        reflection.setCard(front.getBackFaceCard());
        reflection.setTransformed(true);
        reflection.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(reflection);
        return reflection;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
