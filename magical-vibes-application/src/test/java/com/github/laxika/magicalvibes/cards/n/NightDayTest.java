package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.k.KavuMauler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightDay.class, KavuMauler.class})
class NightDayTest extends BaseCardTest {

    @Test
    void nightGivesTargetCreatureMinusOneMinusOneUntilEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstant(player1, 0, 0, List.of(kavu.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void dayGivesAllCreaturesControlledByTargetPlayerPlusOnePlusOne() {
        Permanent ownKavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        Permanent otherKavu = harness.addToBattlefieldAndReturn(player1, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownKavu)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownKavu)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherKavu)).isEqualTo(4);
    }

    @Test
    void modesUseTheirOwnManaCosts() {
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void dayCanTargetItsController() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(5);
    }

    @Test
    void nightCannotTargetAPlayer() {
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dayCannotTargetAcreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(kavu.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dayWearsOffAtEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(4);
    }

    @Test
    void nightWearsOffAtEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new NightDay()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstant(player1, 0, 0, List.of(kavu.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(3);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(4);
    }
}
