package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
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

@CardUsed({TowerDefense.class, DiscipleOfTheOldWays.class})
class TowerDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Tower Defense gives own creatures +0/+5 and reach")
    void buffsOwnCreaturesOnly() {
        Permanent p1a = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent p1b = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent p2 = addCreatureReady(player2, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(p1a.getEffectivePower()).isEqualTo(2);
        assertThat(p1a.getEffectiveToughness()).isEqualTo(7);
        assertThat(p1b.getEffectiveToughness()).isEqualTo(7);
        assertThat(p1a.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.REACH)).isTrue();

        assertThat(p2.getEffectivePower()).isEqualTo(2);
        assertThat(p2.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Tower Defense effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
        assertThat(creature.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        Permanent beforeResolution = addCreatureReady(player1, new DiscipleOfTheOldWays());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new DiscipleOfTheOldWays());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(2);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(7);
        assertThat(beforeResolution.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    void repeatedCastsStackAndBothExpire() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense(), new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(12);
        assertThat(creature.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.REACH)).isFalse();
    }
}
