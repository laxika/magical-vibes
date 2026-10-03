package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlossomingBogbeast.class, GrizzlyBears.class})
class BlossomingBogbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life and boosts all own creatures with trample by life gained this turn")
    void gainsLifeAndBoostsOwnCreatures() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        attackWith(bogbeast);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack boost and trample wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());

        attackWith(bogbeast);

        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bogbeast, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Life lost does not reduce the total life gained used for the boost")
    void lifeLossDoesNotReduceBoost() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test"));

        attackWith(bogbeast);

        harness.assertLife(player1, 22);
        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bogbeast)).isEqualTo(8);
    }

    @Test
    @DisplayName("Each attacking Bogbeast counts life gained by earlier resolving triggers")
    void multipleBogbeastsStackTheirBoosts() {
        Permanent first = addCreatureReady(player1, new BlossomingBogbeast());
        Permanent second = addCreatureReady(player1, new BlossomingBogbeast());
        Permanent nonattacker = addCreatureReady(player1, new BlossomingBogbeast());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        for (Permanent creature : List.of(first, second, nonattacker)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("The boost uses life gained and creatures present when the trigger resolves")
    void evaluatesLifeAndCreaturesAtResolution() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 7));
        Permanent ally = harness.enterBattlefieldAndReturn(player1, new BlossomingBogbeast());

        resolveAllTriggers();

        harness.assertLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Later life gain and creatures entering later do not change the resolved bonus")
    void resolvedBonusIsFixed() {
        Permanent bogbeast = addCreatureReady(player1, new BlossomingBogbeast());
        attackWith(bogbeast);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new BlossomingBogbeast());

        assertThat(gqs.getEffectivePower(gd, bogbeast)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bogbeast)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
    }

    private void attackWith(Permanent creature) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();
    }
}
