package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DukharaPeafowl.class})
@DisplayName("Dukhara Peafowl")
class DukharaPeafowlTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isTrue();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated again after the first activation")
    void canActivateRepeatedly() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability requires blue mana")
    void requiresBlueMana() {
        harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Flying is granted only on resolution and only to the source")
    void grantsFlyingOnlyToSourceOnResolution() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isFalse();
        assertThat(peafowl.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
        assertThat(peafowl.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick Peafowl can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        peafowl.tap();
        peafowl.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isTrue();
        assertThat(peafowl.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the blue activation cost")
    void cannotPayWithColorlessMana() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isFalse();
    }
}
