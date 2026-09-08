package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NobleSteeds.class, AesthirGlider.class})
class NobleSteedsTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W} grants first strike to target creature")
    void grantsFirstStrike() {
        harness.addToBattlefield(player1, new NobleSteeds());
        Permanent bears = addCreatureReady(player1, new AesthirGlider());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        harness.addToBattlefield(player1, new NobleSteeds());
        Permanent bears = addCreatureReady(player1, new AesthirGlider());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void canActivateRepeatedlyAndTargetOpponentCreature() {
        Permanent nobleSteeds = harness.addToBattlefieldAndReturn(player1, new NobleSteeds());
        Permanent ownBears = addCreatureReady(player1, new AesthirGlider());
        Permanent opponentBears = addCreatureReady(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, opponentBears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(nobleSteeds.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Targeting a non-creature permanent is rejected")
    void nonCreatureRejected() {
        harness.addToBattlefield(player1, new NobleSteeds());
        Permanent otherNobleSteeds = harness.addToBattlefieldAndReturn(player1, new NobleSteeds());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, otherNobleSteeds.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
