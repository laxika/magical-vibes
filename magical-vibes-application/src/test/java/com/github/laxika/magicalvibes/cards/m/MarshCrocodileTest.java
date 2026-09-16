package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.cards.n.NightscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshCrocodile.class, StormscapeFamiliar.class, NightscapeFamiliar.class,
        AlphaKavu.class, MoggSentry.class, ManaCylix.class})
class MarshCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a blue or black creature you control to return")
    void etbOffersBlueOrBlackCreatureYouControl() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar()).getId();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new AlphaKavu()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new MoggSentry()).getId();
        harness.addToBattlefield(player1, new ManaCylix());
        harness.addToBattlefield(player2, new StormscapeFamiliar());

        castMarshCrocodile();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        UUID crocodileId = harness.getPermanentId(player1, "Marsh Crocodile");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(blueId, blackId, crocodileId);
        assertThat(choice.validIds()).doesNotContain(greenId, redId);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB returns the chosen creature and makes each player discard")
    void etbReturnsChosenCreatureAndEachPlayerDiscards() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar()).getId();
        harness.setHand(player1, List.of(new MarshCrocodile(), new MoggSentry()));
        harness.setHand(player2, List.of(new AlphaKavu()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, blueId);

        PendingInteraction.DiscardChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.DiscardChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Stormscape Familiar");
        harness.assertInGraveyard(player1, "Mogg Sentry");
        harness.assertInGraveyard(player2, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Marsh Crocodile");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB returns a controlled matching creature to its owner's hand")
    void etbReturnsControlledCreatureToOwnersHand() {
        NightscapeFamiliar ownedByPlayer2 = new NightscapeFamiliar();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, ownedByPlayer2);
        AlphaKavu discardedByPlayer2 = new AlphaKavu();
        harness.setHand(player2, List.of(discardedByPlayer2));

        harness.castFromHand(player1, new MarshCrocodile(), "{2}{U}{B}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, stolenCreature.getId());

        PendingInteraction.DiscardChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        int discardIndex = gd.playerHands.get(player2.getId()).indexOf(discardedByPlayer2);
        harness.handleCardChosen(player2, discardIndex);

        harness.assertInHand(player2, "Nightscape Familiar");
        harness.assertNotInHand(player1, "Nightscape Familiar");
        harness.assertInGraveyard(player2, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Marsh Crocodile");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castMarshCrocodile() {
        harness.castFromHand(player1, new MarshCrocodile(), "{2}{U}{B}");
    }
}
