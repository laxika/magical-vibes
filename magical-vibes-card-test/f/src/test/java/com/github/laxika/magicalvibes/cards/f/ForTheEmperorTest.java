package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForTheEmperor.class, WoodlandDruid.class})
class ForTheEmperorTest extends BaseCardTest {

    @Test
    void includesCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());

        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void repeatedCastsAccumulateBonusesUntilCleanup() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void grantedVigilanceAndLifelinkWorkInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(creature.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("For the Emperor buffs own creatures and grants vigilance and lifelink")
    void buffsOwnCreaturesAndGrantsKeywords() {
        Permanent ownCreature = addCreatureReady(player1, new WoodlandDruid());
        Permanent opponentCreature = addCreatureReady(player2, new WoodlandDruid());

        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownCreature.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(ownCreature.hasKeyword(Keyword.LIFELINK)).isTrue();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("For the Emperor's effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("For the Emperor affects creatures present at resolution only")
    void affectsCreaturesPresentAtResolutionOnly() {
        Permanent existingCreature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new WoodlandDruid());

        assertThat(existingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(existingCreature.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(existingCreature.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(laterCreature.getEffectivePower()).isEqualTo(1);
        assertThat(laterCreature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(laterCreature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
