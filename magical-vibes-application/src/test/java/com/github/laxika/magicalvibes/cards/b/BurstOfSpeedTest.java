package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurstOfSpeed.class, RuneclawBear.class})
class BurstOfSpeedTest extends BaseCardTest {


    @Test
    @DisplayName("Resolving Burst of Speed gives own creatures haste")
    void resolvesAndGrantsHaste() {
        Permanent p1a = addCreatureReady(player1, new RuneclawBear());
        Permanent p1b = addCreatureReady(player1, new RuneclawBear());
        Permanent p2 = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(p1a.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(p2.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Burst of Speed haste wears off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Casting Burst of Speed puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BurstOfSpeed.class);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainHaste() {
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Burst of Speed");
    }

    @Test
    void creaturesEnteringBeforeResolutionGainHaste() {
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void newlyEnteredCreatureCanAttackWithGrantedHaste() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());
        assertThat(creature.isSummoningSick()).isTrue();
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
