package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheNiptonLottery.class, GrizzlyBears.class, SolRing.class})
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

    @Test
    void resolvesWithoutCreaturesAndLeavesNoncreaturesAlone() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        ring.tap();

        castLottery();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ring);
        assertThat(ring.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "The Nipton Lottery");
    }

    @Test
    void soleOpposingCreatureIsAlwaysChosenAndNoncreaturesAreNotDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        creature.tap();
        ring.tap();

        castLottery();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ring);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void soleCreatureAlreadyControlledIsUntappedAndGainsHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        castLottery();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void destroysUnchosenCreaturesOnBothSidesAndPutsThemInOwnersGraveyards() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Permanent> candidates = List.of(first, second, third, fourth);

        castLottery();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent chosen = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(candidates).contains(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(chosen.hasKeyword(Keyword.HASTE)).isTrue();
        for (Permanent candidate : candidates) {
            var owner = candidate == first || candidate == second ? player1 : player2;
            if (candidate == chosen) {
                assertThat(gd.playerGraveyards.get(owner.getId())).doesNotContain(candidate.getCard());
            } else {
                assertThat(gd.playerGraveyards.get(owner.getId())).contains(candidate.getCard());
            }
        }
    }

    private void castLottery() {
        harness.castFromHand(player1, new TheNiptonLottery(), "{2}{B}{R}");
        harness.passBothPriorities();
    }
}
