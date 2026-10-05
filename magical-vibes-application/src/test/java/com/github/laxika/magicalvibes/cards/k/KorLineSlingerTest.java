package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMasses;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorLineSlinger.class, EnormousBaloth.class, HillGiant.class, MightOfTheMasses.class, Plains.class})
class KorLineSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature with power 3 or less")
    void tapsLowPowerCreature() {
        addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Kor Line-Slinger")
    void activatingTapsSelf() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(lineSlinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than 3 is an illegal target")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new KorLineSlinger());
        Permanent giant = addCreatureReady(player2, new EnormousBaloth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTapOwnZeroPowerCreature() {
        addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player1, new KorLineSlinger());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());

        harness.activateAbility(player1, 0, null, lineSlinger.getId());
        harness.passBothPriorities();

        assertThat(lineSlinger.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new KorLineSlinger());
        target.setTapped(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(lineSlinger.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        lineSlinger.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new KorLineSlinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lineSlinger.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        lineSlinger.setTapped(true);
        Permanent target = addCreatureReady(player2, new KorLineSlinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureWhoseEffectivePowerExceedsThree() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lineSlinger.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTapTargetWhosePowerIncreasesAboveThreeInResponse() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(lineSlinger.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lineSlinger.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
