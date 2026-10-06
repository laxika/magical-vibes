package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshalOfZhalfir.class, YouthfulKnight.class, GrizzlyBears.class})
class MarshalOfZhalfirTest extends BaseCardTest {

    @Test
    void twoMarshalsBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void canTapAnotherMarshalYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MarshalOfZhalfir());
        source.setSummoningSick(false);
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickMarshalCannotPayTapCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MarshalOfZhalfir());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Other Knights you control get +1/+1")
    void buffsOtherKnightsYouControl() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent ownNonKnight = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentKnight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKnight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownNonKnight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonKnight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKnight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps another target creature")
    void tapsAnotherTargetCreature() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        marshal.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(marshal.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Marshal of Zhalfir itself")
    void cannotTargetItself() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new MarshalOfZhalfir());
        marshal.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, marshal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
