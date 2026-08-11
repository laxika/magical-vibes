package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForceAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature and offers ferocious draw and discard")
    void returnsCreatureAndOffersFerociousDrawAndDiscard() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer ferocious draw without a creature with power 4 or greater")
    void doesNotOfferFerociousDrawWithoutLargeCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Declining ferocious draw does not discard")
    void decliningFerociousDrawDoesNotDiscard() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceAway(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
