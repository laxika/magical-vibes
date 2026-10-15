package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverlordOfTheMistmoors.class, EncroachingMycosynth.class})
class OverlordOfTheMistmoorsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two flying Insects")
    void enteringCreatesInsects() {
        harness.setHand(player1, List.of(new OverlordOfTheMistmoors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.INSECT))
                .toList();
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().getPower()).isEqualTo(2);
            assertThat(insect.getCard().getToughness()).isEqualTo(1);
            assertThat(insect.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(gqs.hasKeyword(gd, insect, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Casting with impending enters with time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @DisplayName("Attacking creates two more flying Insects")
    void attackingCreatesInsects() {
        Permanent overlord = addCreatureReady(player1, new OverlordOfTheMistmoors());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.INSECT)))
                .hasSize(2);
        assertThat(overlord.isAttackedThisTurn()).isTrue();
    }

    @Test
    void impendingStillCreatesTwoFlyingInsects() {
        castWithImpending();

        List<Permanent> insects = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(insect ->
                assertThat(gqs.hasKeyword(gd, insect, Keyword.FLYING)).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void impendingRemovesOnlyOneCounterPerOwnEndStep() {
        Permanent overlord = castWithImpending();

        for (int remaining = 3; remaining >= 0; remaining--) {
            advanceToOwnEndStep();
            assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(remaining);
            assertThat(gqs.isCreature(gd, overlord)).isEqualTo(remaining == 0);
        }
        advanceToOwnEndStep();
        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void opponentsEndStepDoesNotRemoveTimeCounter() {
        Permanent overlord = castWithImpending();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void normallyCastOverlordIgnoresTimeCounters() {
        harness.setHand(player1, List.of(new OverlordOfTheMistmoors()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent overlord = findPermanent(player1, "Overlord of the Mistmoors");
        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        overlord.setCounterCount(CounterType.TIME, 2);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @CardUsed({EncroachingMycosynth.class})
    void impendingPreservesArtifactTypeGrantedByOlderMycosynth() {
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        Permanent overlord = castWithImpending();

        assertThat(gqs.isArtifact(gd, overlord)).isTrue();
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheMistmoors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        return findPermanent(player1, "Overlord of the Mistmoors");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
