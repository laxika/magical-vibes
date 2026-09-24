package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FableOfTheMirrorBreaker.class, ReflectionOfKikiJiki.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addSaga(1);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardIndexChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardIndexChosen(0));

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(reflection), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
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
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int reflectionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reflection);

        assertThatThrownBy(() -> harness.activateAbility(player1, reflectionIndex, 0, null, reflection.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonlegendary creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, reflectionIndex, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonlegendary creature you control");
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
