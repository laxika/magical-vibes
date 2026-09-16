package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MysticPenitent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantSuccession.class, Firebolt.class, DruidLyrist.class, MysticPenitent.class, Forest.class})
class VerdantSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("A green nontoken creature's controller may search for a same-named card")
    void dyingCreatureControllerMaySearchForSameName() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new DruidLyrist(), new Forest()));
        prepareRemoval(lyrist);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().extracting(Card::getName).isEqualTo("Druid Lyrist");

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).hasSize(1);
        assertThat(findPermanents(player2, "Druid Lyrist").getFirst().isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> "Druid Lyrist".equals(card.getName()));
    }

    @Test
    @DisplayName("The dying creature's controller may decline the search")
    void searchMayBeDeclined() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new DruidLyrist()));
        prepareRemoval(lyrist);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).isEmpty();
    }

    @Test
    @DisplayName("The search completes without a card when no same-named card is in the library")
    void searchWithNoMatchingCardCompletes() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent lyrist = harness.addToBattlefieldAndReturn(player2, new DruidLyrist());
        harness.setLibrary(player2, List.of(new Forest()));
        prepareRemoval(lyrist);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Druid Lyrist")).isEmpty();
    }

    @Test
    @DisplayName("Only green nontoken creature deaths trigger the ability")
    void nonGreenAndTokenDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new VerdantSuccession());
        Permanent penitent = harness.addToBattlefieldAndReturn(player2, new MysticPenitent());
        prepareRemoval(penitent);
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());
        prepareRemoval(token);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareRemoval(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();
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
