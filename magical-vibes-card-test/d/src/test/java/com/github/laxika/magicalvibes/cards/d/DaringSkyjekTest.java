package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaringSkyjek.class})
class DaringSkyjekTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain flying when attacking with fewer than two other creatures")
    void noFlyingWithFewerThanTwoOtherAttackers() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying when attacking with two other creatures")
    void gainsFlyingWithTwoOtherAttackers() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking alone does not trigger battalion")
    void attackingAloneDoesNotTriggerBattalion() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Skyjek that stays back does not gain flying when three others attack")
    void nonattackingSkyjekDoesNotGainFlying() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        Permanent attacker = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Battalion resolves even if the creatures have left combat")
    void battalionDoesNotRecheckAttackingCreaturesAtResolution() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        Permanent second = addCreatureReady(player1, new DaringSkyjek());
        Permanent third = addCreatureReady(player1, new DaringSkyjek());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));

        assertThat(gd.stack).hasSize(3);
        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();

        skyjek.setAttacking(false);
        second.setAttacking(false);
        third.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.FLYING)).isTrue();
    }
}
