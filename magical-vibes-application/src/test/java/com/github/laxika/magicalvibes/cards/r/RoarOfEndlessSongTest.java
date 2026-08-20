package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoarOfEndlessSongTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 5/5 green Elephant token")
    void chapterICreatesElephantToken() {
        harness.addToBattlefield(player1, new RoarOfEndlessSong());
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent elephant = findElephants().getFirst();
        assertThat(elephant.getEffectivePower()).isEqualTo(5);
        assertThat(elephant.getEffectiveToughness()).isEqualTo(5);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Chapter II creates another 5/5 green Elephant token")
    void chapterIICreatesElephantToken() {
        harness.addToBattlefield(player1, new RoarOfEndlessSong());
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findElephants()).hasSize(1);
        assertThat(findElephants().getFirst().getEffectivePower()).isEqualTo(5);
        assertThat(findElephants().getFirst().getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Chapter III doubles your creatures' power and toughness until end of turn")
    void chapterIIIDoublesOwnCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new RoarOfEndlessSong());
        Permanent saga = findSaga();
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findSaga() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RoarOfEndlessSong)
                .findFirst()
                .orElseThrow();
    }

    private java.util.List<Permanent> findElephants() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
