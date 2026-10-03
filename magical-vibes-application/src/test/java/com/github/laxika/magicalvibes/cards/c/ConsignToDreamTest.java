package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TattermungeManiac;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsignToDream.class, FugitiveWizard.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Ornithopter.class, TattermungeManiac.class, CeruleanWisps.class})
class ConsignToDreamTest extends BaseCardTest {

    private void castOn(UUID targetId) {
        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Blue permanent is returned to its owner's hand")
    void bluePermanentReturnedToHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInHand(player2, "Fugitive Wizard");
        // Not put on library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Colorless permanent is returned to its owner's hand")
    void colorlessPermanentReturnedToHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();

        castOn(targetId);

        harness.assertInHand(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Noncreature permanent is returned to its owner's hand")
    void noncreaturePermanentReturnedToHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        castOn(targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Green permanent is put on top of its owner's library instead")
    void greenPermanentPutOnTopOfLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Red permanent is put on top of its owner's library instead")
    void redPermanentPutOnTopOfLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotInHand(player2, "Hill Giant");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Consign to Dream");
    }

    @Test
    @DisplayName("Red-green hybrid permanent goes to the library exactly once")
    void hybridPermanentPutOnLibraryOnce() {
        TattermungeManiac card = new TattermungeManiac();
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        Forest bottomCard = new Forest();
        harness.setLibrary(player2, List.of(bottomCard));

        castOn(target.getId());

        harness.assertNotOnBattlefield(player2, "Tattermunge Maniac");
        harness.assertNotInHand(player2, "Tattermunge Maniac");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card, bottomCard);
    }

    @Test
    @DisplayName("A stolen green permanent goes to its owner's library")
    void stolenGreenPermanentGoesToOwnerLibrary() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        int controllerLibrarySize = gd.playerDecks.get(player2.getId()).size();

        castOn(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(card);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerLibrarySize);
    }

    @Test
    @DisplayName("A stolen blue permanent goes to its owner's hand")
    void stolenBluePermanentGoesToOwnerHand() {
        FugitiveWizard card = new FugitiveWizard();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);

        castOn(target.getId());

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        harness.assertNotInHand(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Color is checked at resolution after a responding color change")
    void usesColorAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TattermungeManiac());
        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new CeruleanWisps()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tattermunge Maniac");
        harness.assertInHand(player2, "Tattermunge Maniac");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
    }
}
