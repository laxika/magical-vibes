package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnhancedSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("May look at two additional cards while surveilling")
    void mayLookAtAdditionalCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new GrizzlyBears();
        Card second = new Island();
        Card third = new GrizzlyBears();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second, third, fourth);
    }

    @Test
    @DisplayName("May decline to look at additional cards")
    void mayDeclineAdditionalCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        Card first = new GrizzlyBears();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
    }

    @Test
    @DisplayName("Exiles itself and shuffles its controller's graveyard into their library")
    void exilesSelfAndShufflesGraveyardIntoLibrary() {
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 2);
        harness.assertNotOnBattlefield(player1, "Enhanced Surveillance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Enhanced Surveillance"));
    }
}
