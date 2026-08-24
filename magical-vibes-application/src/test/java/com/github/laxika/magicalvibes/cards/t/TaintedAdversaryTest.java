package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TaintedAdversary.class)
class TaintedAdversaryTest extends BaseCardTest {

    @Test
    void paysMultipleTimesAndCreatesTwiceAsManyDecayedZombies() {
        harness.setHand(player1, List.of(new TaintedAdversary()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent adversary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(4).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        });
    }

    @Test
    void mayDeclineWithoutAddingCountersOrCreatingTokens() {
        harness.setHand(player1, List.of(new TaintedAdversary()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent adversary = battlefield.stream().findFirst().orElseThrow();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(battlefield).noneMatch(permanent -> permanent.getCard().isToken());
    }
}
