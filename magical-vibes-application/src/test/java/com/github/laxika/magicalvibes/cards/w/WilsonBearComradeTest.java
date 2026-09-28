package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WilsonBearComrade.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class})
class WilsonBearComradeTest extends BaseCardTest {

    @Test
    void baseFaceHasReachTrampleAndWard() {
        Permanent wilson = addCreatureReady(player1, new WilsonBearComrade());

        assertThat(gqs.hasKeyword(gd, wilson, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.WARD)).isTrue();
    }

    @Test
    void whiteFaceHasLifelinkAndCorrectPowerToughness() {
        Permanent wilson = specialize(CardColor.WHITE, 0, new Plains());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Urbane Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(4);
    }

    @Test
    void blueFaceCannotBeBlocked() {
        Permanent wilson = specialize(CardColor.BLUE, 1, new Island());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Subtle Bear");
        assertThat(gqs.hasCantBeBlocked(gd, wilson)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(3);
    }

    @Test
    void blackFaceHasMenace() {
        Permanent wilson = specialize(CardColor.BLACK, 2, new Swamp());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Fearsome Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(4);
    }

    @Test
    void redFaceHasDoubleStrike() {
        Permanent wilson = specialize(CardColor.RED, 3, new Mountain());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Ardent Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(3);
    }

    @Test
    void greenFaceHasFiveFiveBody() {
        Permanent wilson = specialize(CardColor.GREEN, 4, new Forest());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Majestic Bear");
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(5);
    }

    @Test
    void greenGraveyardAbilityPerpetuallyBoostsAndGrantsKeywords() {
        Permanent wilson = specialize(CardColor.GREEN, 4, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wilson));

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int wilsonGraveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(wilson.getCard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateGraveyardAbility(player1, wilsonGraveyardIndex, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.WARD)).isTrue();
    }

    private Permanent specialize(CardColor color, int abilityIndex, Card discard) {
        Permanent wilson = addCreatureReady(player1, new WilsonBearComrade());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return wilson;
    }
}
