package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sailmonger.class, Mountain.class})
class SailmongerTest extends BaseCardTest {
    @Test
    void opponentCanActivateSailmongerTheyDoNotControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player2, 0, null, source.getId());
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
    }

    @Test
    void opponentMustPayEvenWhenControllerHasEnoughMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void tappedSummoningSickSailmongerCanActivateRepeatedly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sailmonger());
        source.tap();
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void anyPlayerMayPayToGrantFlyingToTargetCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sailmonger());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
    }

    @Test
    void abilityRequiresTwoGenericMana() {
        harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Sailmonger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedFlyingWearsOffAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void abilityCanOnlyTargetCreatures() {
        harness.addToBattlefieldAndReturn(player1, new Sailmonger());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
