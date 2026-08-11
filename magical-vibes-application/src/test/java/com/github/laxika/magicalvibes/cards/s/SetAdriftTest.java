package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetAdriftTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and puts a nonland permanent on top of its owner's library")
    void delvesAndPutsNonlandPermanentOnTop() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());

        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SetAdrift()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, targetId, List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Set Adrift");
    }

    @Test
    @DisplayName("Can target a noncreature nonland permanent")
    void canTargetNoncreatureNonlandPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new Pacifism());
        UUID pacifismId = harness.getPermanentId(player2, "Pacifism");
        Permanent pacifism = gqs.findPermanentById(gd, pacifismId);
        pacifism.setAttachedTo(bearsId);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new SetAdrift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0, pacifismId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pacifism");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new SetAdrift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new SetAdrift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Set Adrift");
    }
}
