package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkromasBlessing.class, GrizzlyBears.class, SuntailHawk.class})
class AkromasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures you control gain protection from the chosen color until end of turn")
    void grantsProtectionToOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AkromasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(ownCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownOtherCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(opposingCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AkromasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cycling {W} discards Akroma's Blessing and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AkromasBlessing()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Akroma's Blessing");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
