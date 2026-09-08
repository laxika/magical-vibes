package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgelessSentinels.class, GiantSpider.class})
class AgelessSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("When Ageless Sentinels blocks, it becomes a Bird Giant and loses defender")
    void blockingChangesCreatureTypesAndRemovesDefender() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent sentinels = addCreatureReady(player2, new AgelessSentinels());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sentinels))
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(4);
    }

    @Test
    @DisplayName("The subtype change and defender loss last beyond the current turn")
    void changesPersistBeyondEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent sentinels = addCreatureReady(player2, new AgelessSentinels());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sentinels))
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Ageless Sentinels does not trigger when it does not block")
    void doesNotTriggerWhenItDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new AgelessSentinels());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
