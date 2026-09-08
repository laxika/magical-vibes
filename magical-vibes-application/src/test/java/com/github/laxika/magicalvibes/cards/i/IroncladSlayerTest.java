package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IroncladSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted Aura card from the graveyard to hand")
    void returnsAuraFromGraveyardToHand() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(oakenform.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakenform");
        harness.assertNotInGraveyard(player1, "Oakenform");
    }

    @Test
    @DisplayName("ETB returns a targeted Equipment card from the graveyard to hand")
    void returnsEquipmentFromGraveyardToHand() {
        Bonesplitter bonesplitter = new Bonesplitter();
        harness.setGraveyard(player1, List.of(bonesplitter));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bonesplitter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertNotInGraveyard(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("ETB cannot target a non-Aura, non-Equipment card")
    void doesNotTargetOtherCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB may decline to return a card")
    void mayDeclineToReturnCard() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castIroncladSlayer();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oakenform");
        harness.assertNotInHand(player1, "Oakenform");
    }

    private void castIroncladSlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IroncladSlayer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
