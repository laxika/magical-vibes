package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnchorToTheAether.class, TimberpackWolf.class, Forest.class})
class AnchorToTheAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts the target creature on top of its owner's library and offers scry 1")
    void resolvingTucksTargetAndScries() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        harness.assertNotInGraveyard(player2, "Timberpack Wolf");

        List<Card> victimDeck = gd.playerDecks.get(player2.getId());
        assertThat(victimDeck).hasSize(deckSizeBefore + 1);
        assertThat(victimDeck.getFirst().getName()).isEqualTo("Timberpack Wolf");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 can bottom the revealed card")
    void scryBottomsTheCard() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");

        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(originalTop);
        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Anchor to the Aether");
    }

    @Test
    @DisplayName("Scry 1 can keep the revealed card on top")
    void scryKeepsCardOnTop() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");

        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.getFirst()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returning your own creature happens before scry, allowing it to be bottomed")
    void ownCreatureCanBeBottomedByScry() {
        Card creature = new TimberpackWolf();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, creature).getId();
        Card previousTop = new Forest();
        harness.setLibrary(player1, List.of(previousTop));
        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(creature);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertNotOnBattlefield(player1, "Timberpack Wolf");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(previousTop, creature);
        harness.assertInGraveyard(player1, "Anchor to the Aether");
    }

    @Test
    @DisplayName("An empty caster library does not prevent returning the creature")
    void emptyLibraryStillReturnsCreature() {
        Card creature = new TimberpackWolf();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Anchor to the Aether");
    }

    @Test
    @DisplayName("No scry occurs when the sole target leaves before resolution")
    void missingTargetPreventsScry() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new AnchorToTheAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Anchor to the Aether");
    }
}
