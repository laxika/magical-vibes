package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildShape.class, HillGiantHerdgorger.class, Island.class, WhiteDragon.class})
class WildShapeTest extends BaseCardTest {

    @Test
    void turnsTargetIntoAHexproofTurtle() {
        Permanent target = cast(0);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.TURTLE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void turnsTargetIntoAReachSpider() {
        Permanent target = cast(1);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.SPIDER);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    void turnsTargetIntoATrampleElephant() {
        Permanent target = cast(2);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.ELEPHANT);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent target = cast(0);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void canOnlyTargetACreatureYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent cast(int modeIndex) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of(target.getId()));
        harness.passBothPriorities();
        return target;
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laterShapeReplacesBaseStatsAndTypeButRetainsPreviouslyGrantedAbilities() {
        Permanent target = cast(0);
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.ELEPHANT);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void plusOneCountersStillApplyToTheNewBaseStats() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.SPIDER);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isFalse();
    }

    @Test
    void retainsTheCreaturesOriginalAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhiteDragon());
        harness.setHand(player1, List.of(new WildShape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.TURTLE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }
}
