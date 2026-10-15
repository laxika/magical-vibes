package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LeapingMaster.class})
class LeapingMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants flying until end of turn")
    void grantsFlying() {
        Permanent leapingMaster = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent leapingMaster = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted only on resolution and only to the activating creature")
    void grantsFlyingOnlyToSourceOnResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LeapingMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while summoning sick without tapping")
    void activatesWhileSummoningSickWithoutTapping() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating requires white mana")
    void rejectsManaWithoutWhite() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
    }
}
