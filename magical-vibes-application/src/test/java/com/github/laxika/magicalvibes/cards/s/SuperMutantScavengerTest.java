package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
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

@CardUsed({SuperMutantScavenger.class, Oakenform.class, Bonesplitter.class, GrizzlyBears.class})
class SuperMutantScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted Aura card from the graveyard to hand")
    void returnsAuraOnEntering() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castScavenger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(oakenform.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakenform");
        harness.assertNotInGraveyard(player1, "Oakenform");
    }

    @Test
    @DisplayName("Death trigger returns a targeted Equipment card from the graveyard to hand")
    void returnsEquipmentOnDeath() {
        Bonesplitter bonesplitter = new Bonesplitter();
        harness.setGraveyard(player1, List.of(bonesplitter));
        Permanent scavenger = addCreatureReady(player1, new SuperMutantScavenger());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, scavenger));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bonesplitter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertNotInGraveyard(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("Triggers cannot target a non-Aura, non-Equipment card")
    void doesNotTargetOtherCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger may decline to return a card")
    void mayDeclineToReturnCard() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castScavenger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oakenform");
        harness.assertNotInHand(player1, "Oakenform");
    }

    private void castScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SuperMutantScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
