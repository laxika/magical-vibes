package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryMice.class, Forest.class, GrizzlyBears.class})
class ArmoryMiceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+2 after two nonland permanents enter under your control this turn")
    void getsToughnessBoostAfterTwoNonlandPermanentsEnter() {
        Permanent mice = castArmoryMice();

        assertThat(gqs.getEffectivePower(gd, mice)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mice)).isEqualTo(1);

        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, mice)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mice)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent mice = castArmoryMice();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        assertThat(gqs.getEffectiveToughness(gd, mice)).isEqualTo(1);
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent mice = castArmoryMice();
        castGrizzlyBears();

        assertThat(gqs.getEffectiveToughness(gd, mice)).isEqualTo(3);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, mice)).isEqualTo(1);
    }

    private Permanent castArmoryMice() {
        harness.setHand(player1, List.of(new ArmoryMice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Armory Mice");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
