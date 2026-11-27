package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecondLittlePig.class, Clone.class})
class SecondLittlePigTest extends BaseCardTest {

    @Test
    void whiteManaTransformsPigWithoutTappingIt() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertSpiritTransformation(pig);
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    void blackManaTransformsTappedPigAndTransformationSurvivesCleanup() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        pig.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertSpiritTransformation(pig);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertSpiritTransformation(pig);
    }

    @Test
    void cannotActivateAfterBecomingSpirit() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertSpiritTransformation(pig);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A copy of the transformed Pig copies its printed values, not its perpetual flying")
    void copyDoesNotCopyPerpetualFlying() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Clone()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, pig.getId());

        Permanent copy = findPermanent(player2, "Second Little Pig");
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pig, Keyword.FLYING)).isTrue();
    }

    private void assertSpiritTransformation(Permanent pig) {
        assertThat(gqs.getEffectivePower(gd, pig)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, pig)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, pig, CardSubtype.BOAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, pig, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasKeyword(gd, pig, Keyword.FLYING)).isTrue();
    }
}
