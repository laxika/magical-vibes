package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FledglingImp.class, DuskImp.class})
class FledglingImpTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, Discard a card gives this creature flying until end of turn")
    void discardGrantsFlying() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new FledglingImp());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Imp");
        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new FledglingImp());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new FledglingImp());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new FledglingImp());
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dusk Imp");
        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isFalse();
    }
}
