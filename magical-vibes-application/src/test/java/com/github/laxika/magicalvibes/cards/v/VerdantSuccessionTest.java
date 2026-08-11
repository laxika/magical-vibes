package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdantSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("A green nontoken creature's controller may search for a same-named card")
    void dyingCreatureControllerMaySearchForSameName() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setDeck(player2, List.of(new GrizzlyBears(), new Forest()));
        prepareRemoval(bears);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().extracting(Card::getName).isEqualTo("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("The dying creature's controller may decline the search")
    void searchMayBeDeclined() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setDeck(player2, List.of(new GrizzlyBears()));
        prepareRemoval(bears);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(named(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Only green nontoken creature deaths trigger the ability")
    void nonGreenAndTokenDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareRemoval(hillGiant);
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());
        prepareRemoval(token);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareRemoval(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private List<Permanent> named(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .toList();
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Saproling Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
