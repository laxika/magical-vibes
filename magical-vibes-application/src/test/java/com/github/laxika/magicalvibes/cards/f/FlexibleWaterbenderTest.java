package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GliderStaff;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlexibleWaterbender.class, Plains.class})
class FlexibleWaterbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps three artifacts or creatures and sets this creature to 5/2")
    void waterbendSetsBasePowerAndToughness() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(5);
    }

    @Test
    @CardUsed(GliderStaff.class)
    void waterbendCanTapNoncreatureArtifacts() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new GliderStaff());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new GliderStaff());

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(firstArtifact.isTapped()).isTrue();
        assertThat(secondArtifact.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(2);
    }

    @Test
    void waterbendCanBePaidEntirelyWithManaWhileSourceIsTapped() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        waterbender.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(2);
    }

    @Test
    void waterbendCanCombineManaAndSummoningSickCreatures() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        waterbender.setSummoningSick(true);
        helper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(2);
    }

    @Test
    void waterbendCannotUseTappedCreaturesOpposingCreaturesOrOrdinaryLands() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        tappedCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FlexibleWaterbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(waterbender.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedWaterbendingDoesNotOverwriteOrMultiplyCounters() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        waterbender.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(3);
        assertThat(waterbender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(6);
    }
}
