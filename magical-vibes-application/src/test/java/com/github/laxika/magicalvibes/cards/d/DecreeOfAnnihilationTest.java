package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfAnnihilation.class, GrizzlyBears.class, GloriousAnthem.class, Island.class, SolRing.class})
class DecreeOfAnnihilationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles artifacts, creatures, lands, graveyards, and hands")
    void spellExilesTheSpecifiedZonesAndPermanentTypes() {
        Card decree = new DecreeOfAnnihilation();
        Card ownCreature = new GrizzlyBears();
        Card ownArtifact = new SolRing();
        Card ownLandInHand = new Island();
        Card ownGraveyardCard = new GrizzlyBears();
        Card opponentLand = new Island();
        Card opponentHandCard = new GrizzlyBears();
        Card opponentGraveyardCard = new Island();
        Card anthem = new GloriousAnthem();

        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player1, ownArtifact);
        harness.addToBattlefield(player1, anthem);
        harness.addToBattlefield(player2, opponentLand);
        harness.setHand(player1, List.of(decree, ownLandInHand));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(anthem.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownCreature.getId(), ownArtifact.getId(), ownLandInHand.getId(),
                        ownGraveyardCard.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(opponentLand.getId(), opponentHandCard.getId(),
                        opponentGraveyardCard.getId());
        harness.assertInGraveyard(player1, "Decree of Annihilation");
    }

    @Test
    @DisplayName("Cycling destroys all lands and still draws a card")
    void cyclingDestroysLandsAndDraws() {
        Card decree = new DecreeOfAnnihilation();
        Card draw = new GrizzlyBears();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(decree));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Decree of Annihilation");
    }
}
