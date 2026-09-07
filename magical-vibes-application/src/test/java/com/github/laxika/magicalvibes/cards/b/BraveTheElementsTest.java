package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BraveTheElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Only white creatures you control gain protection from the chosen color")
    void grantsProtectionToOwnWhiteCreaturesOnly() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(opponentHawk.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        hawk.resetModifiers();
        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("With no white creatures the spell still requires its color choice")
    void stillChoosesColorWithoutWhiteCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
    }
}
