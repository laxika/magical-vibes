package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgelessSentinels.class, ScornfulEgotist.class})
class AgelessSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("When Ageless Sentinels blocks, it becomes a Bird Giant and loses defender")
    void blockingChangesCreatureTypesAndRemovesDefender() {
        addCreatureReady(player1, new ScornfulEgotist());
        Permanent sentinels = addCreatureReady(player2, new AgelessSentinels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sentinels))
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("The subtype change and defender loss last beyond the current turn")
    void changesPersistBeyondEndOfTurn() {
        addCreatureReady(player1, new ScornfulEgotist());
        Permanent sentinels = addCreatureReady(player2, new AgelessSentinels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(als.canAttack(gd, sentinels, player2.getId())).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sentinels))
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.DEFENDER)).isFalse();
        assertThat(als.canAttack(gd, sentinels, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Ageless Sentinels does not trigger when it does not block")
    void doesNotTriggerWhenItDoesNotBlock() {
        addCreatureReady(player1, new ScornfulEgotist());
        Permanent sentinels = addCreatureReady(player2, new AgelessSentinels());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sentinels))
                .containsExactly(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.DEFENDER)).isTrue();
        assertThat(als.canAttack(gd, sentinels, player2.getId())).isFalse();
    }
}
