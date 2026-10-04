package com.github.laxika.magicalvibes.cards.b;

import java.util.List;

import com.github.laxika.magicalvibes.cards.i.ImperialMoth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BefriendingTheMoths.class, ImperialMoth.class, BearerOfMemory.class})
class BefriendingTheMothsTest extends BaseCardTest {

    @Test
    void chapterIAndIIBoostAndGrantFlyingToTargetCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        castSaga();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(opposingCreature.getPowerModifier()).isZero();

        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter(player1);

        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void chapterIIIExilesSagaAndReturnsItTransformed() {
        BefriendingTheMoths card = new BefriendingTheMoths();
        card.setOwnerId(player1.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player2, card);
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter(player2);
        harness.passBothPriorities();

        Permanent transformedSaga = findSaga(player2);
        assertThat(transformedSaga.isTransformed()).isTrue();
        assertThat(transformedSaga.getCard()).isSameAs(transformedSaga.getOriginalCard().getBackFaceCard());
    }

    @Test
    void chapterBoostAndFlyingExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        castSaga();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void chapterDoesNotAffectCreatureNoLongerControlledByAbilityController(int chapter) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        if (chapter == 1) {
            castSaga();
        } else {
            Permanent saga = harness.addToBattlefieldAndReturn(player1, new BefriendingTheMoths());
            saga.setCounterCount(CounterType.LORE, 1);
            advanceToNextChapter(player1);
        }
        harness.handlePermanentChosen(player1, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void chapterDoesNotRedirectWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        castSaga();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnedMothIsNewObjectAndCannotAttackUntilNextTurn() {
        harness.setHand(player2, List.of());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BefriendingTheMoths());
        saga.setCounterCount(CounterType.LORE, 2);
        saga.tap();
        advanceToNextChapter(player1);
        harness.passBothPriorities();

        Permanent moth = findSaga();
        assertThat(moth.getId()).isNotEqualTo(saga.getId());
        assertThat(moth.isTransformed()).isTrue();
        assertThat(moth.isTapped()).isFalse();
        assertThat(moth.getCounterCount(CounterType.LORE)).isZero();
        assertThat(als.canAttack(gd, moth, player1.getId())).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(als.canAttack(gd, moth, player1.getId())).isTrue();
    }

    @Test
    void sagaContinuesWithoutAnyCreatureYouControlToTarget() {
        harness.addToBattlefield(player2, new BearerOfMemory());
        castSaga();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findSaga().getCounterCount(CounterType.LORE)).isEqualTo(1);

        advanceToNextChapter(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findSaga().getCounterCount(CounterType.LORE)).isEqualTo(2);

        advanceToNextChapter(player1);
        harness.passBothPriorities();

        assertThat(findSaga().isTransformed()).isTrue();
    }

    private void castSaga() {
        harness.castFromHand(player1, new BefriendingTheMoths(), "{3}{W}");
        harness.passBothPriorities();
    }

    private Permanent findSaga() {
        return findSaga(player1);
    }

    private Permanent findSaga(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BefriendingTheMoths)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
