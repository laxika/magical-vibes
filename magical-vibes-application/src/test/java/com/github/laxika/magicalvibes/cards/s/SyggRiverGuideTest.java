package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyggRiverGuideTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W}: a targeted Merfolk you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID syggId = harness.getPermanentId(player1, "Sygg, River Guide");
        harness.activateAbility(player1, 0, null, syggId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player1, "RED");

        Permanent sygg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(syggId)).findFirst().orElseThrow();
        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID syggId = harness.getPermanentId(player1, "Sygg, River Guide");
        harness.activateAbility(player1, 0, null, syggId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent sygg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(syggId)).findFirst().orElseThrow();
        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sygg.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("The ability cannot target a non-Merfolk you control")
    void cannotTargetNonMerfolk() {
        harness.addToBattlefield(player1, new SyggRiverGuide());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
