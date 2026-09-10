package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CascadeSeer.class, BoggartBrute.class, FaerieMiscreant.class, Forest.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class CascadeSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Scry amount equals the size of its controller's party")
    void scriesForPartySize() {
        addFullParty();
        List<Card> library = List.of(new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castCascadeSeer();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyElementsOf(library);

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gameData.playerDecks.get(player1.getId()))
                .containsSubsequence(library.get(3), library.get(2), library.get(1), library.get(0));
    }

    @Test
    @DisplayName("A party creature can fill only one role")
    void oneCreatureFillsOnlyOneRole() {
        harness.addToBattlefield(player1, partyCreature("Cleric Rogue", CardSubtype.CLERIC, CardSubtype.ROGUE));
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        List<Card> library = List.of(new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);
        castCascadeSeer();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyElementsOf(library);
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castCascadeSeer() {
        harness.setHand(player1, List.of(new CascadeSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private Card partyCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
