package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BefriendingTheMoths.class, GrizzlyBears.class})
class BefriendingTheMothsTest extends BaseCardTest {

    @Test
    void chapterIAndIIBoostAndGrantFlyingToTargetCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private void castSaga() {
        harness.setHand(player1, List.of(new BefriendingTheMoths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
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
