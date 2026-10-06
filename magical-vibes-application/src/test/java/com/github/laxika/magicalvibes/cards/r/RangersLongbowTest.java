package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RangersLongbow.class, GrizzlyBears.class, DireWolfProwler.class})
class RangersLongbowTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        longbow.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void equipAttachesLongbowToCreatureYouControl() {
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(longbow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reequippingMovesAllBenefitsOnlyWhenAbilityResolves() {
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(longbow.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isFalse();

        harness.passBothPriorities();

        assertThat(longbow.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    void equipRequiresThreeMana() {
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(longbow.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetAnotherNoncreatureEquipment() {
        harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        Permanent otherLongbow = harness.addToBattlefieldAndReturn(player1, new RangersLongbow());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherLongbow.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
