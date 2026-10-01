package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasaltGargoyle;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThickSkinnedGoblin.class, BasaltGargoyle.class, SuddenShock.class})
class ThickSkinnedGoblinTest extends BaseCardTest {

    @Test
    void mayPayZeroForEcho() {
        harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Basalt Gargoyle");
    }

    @Test
    void mayDeclineEchoAndSacrifice() {
        harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.castFromHand(player1, new BasaltGargoyle(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Thick-Skinned Goblin");
        harness.assertInGraveyard(player1, "Basalt Gargoyle");
    }

    @Test
    void activatedAbilityGrantsProtectionUntilEndOfTurn() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    void protectionFromRedPreventsRedSpellTargeting() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new ThickSkinnedGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
