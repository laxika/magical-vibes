package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheNiptonLottery.class, GrizzlyBears.class})
class TheNiptonLotteryTest extends BaseCardTest {

    @Test
    void randomlyChosenCreatureIsStolenUntappedAndHastyWhileOtherCreatureDies() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.tap();
        second.tap();

        castLottery();

        List<Permanent> survivingCreatures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(first.getId())
                        || permanent.getId().equals(second.getId()))
                .toList();
        assertThat(survivingCreatures).hasSize(1);
        Permanent chosen = survivingCreatures.getFirst();
        assertThat(chosen.isTapped()).isFalse();
        assertThat(chosen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(chosen.getId())).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId())
                        || permanent.getId().equals(second.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void chosenCreatureAndHasteReturnAtCleanup() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLottery();

        UUID chosenId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(first.getId())
                        || permanent.getId().equals(second.getId()))
                .map(Permanent::getId)
                .findFirst()
                .orElseThrow();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(chosenId))
                .findFirst()
                .orElseThrow();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(chosenId)).isFalse();
    }

    private void castLottery() {
        harness.setHand(player1, List.of(new TheNiptonLottery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
