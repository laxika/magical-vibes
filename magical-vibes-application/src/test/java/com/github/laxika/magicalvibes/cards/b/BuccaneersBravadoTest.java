package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuccaneersBravadoTest extends BaseCardTest {

    @Test
    void firstStrikeModeBoostsAnyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        castBravado(0, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doubleStrikeModeBoostsPirate() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new SailorOfMeans());
        int basePower = gqs.getEffectivePower(gd, pirate);
        int baseToughness = gqs.getEffectiveToughness(gd, pirate);

        castBravado(1, pirate);

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, pirate, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doubleStrikeModeCannotTargetNonPirateCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BuccaneersBravado()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pirate");
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBravado(0, creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castBravado(int mode, Permanent target) {
        harness.setHand(player1, List.of(new BuccaneersBravado()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
