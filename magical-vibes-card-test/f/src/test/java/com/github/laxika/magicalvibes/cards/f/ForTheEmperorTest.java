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

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void repeatedCastsAccumulateBonusesUntilCleanup() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
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

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();
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

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("For the Emperor affects creatures present at resolution only")
    void affectsCreaturesPresentAtResolutionOnly() {
        Permanent existingCreature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new ForTheEmperor(), "{3}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new WoodlandDruid());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.LIFELINK)).isFalse();
    }
}
