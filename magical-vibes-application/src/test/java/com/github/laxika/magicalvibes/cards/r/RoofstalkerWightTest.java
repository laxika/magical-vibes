package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed(RoofstalkerWight.class)
class RoofstalkerWightTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability requires two mana including blue")
    void requiresMana() {
        harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability requires the generic portion in addition to its blue mana")
    void requiresGenericMana() {
        harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability cannot be paid without blue mana")
    void requiresBlueMana() {
        harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while Roofstalker Wight is tapped")
    void doesNotRequireTapping() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        wight.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wight.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying is granted only on resolution and only to the ability's source")
    void grantsFlyingOnlyToSourceOnResolution() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        Permanent otherWight = harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        Permanent opposingWight = harness.addToBattlefieldAndReturn(player2, new RoofstalkerWight());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWight, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWight, Keyword.FLYING)).isFalse();
        assertThat(wight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activating the flying ability")
    void canActivateWithSummoningSickness() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new RoofstalkerWight());
        wight.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wight, Keyword.FLYING)).isTrue();
        assertThat(wight.isTapped()).isFalse();
    }
}
