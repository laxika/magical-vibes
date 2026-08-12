package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

class NissaRevaneTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts Nissa's Chosen from the library onto the battlefield")
    void plusOneFindsNissasChosen() {
        Permanent nissa = addReadyNissa(player1, 2);
        Card chosen = nissasChosen();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(chosen);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Nissa's Chosen"));
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 gains 2 life for each Elf controlled")
    void plusOneGainsLifeForEachElf() {
        Permanent nissa = addReadyNissa(player1, 2);
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-7 puts any number of Elf creature cards from the library onto the battlefield")
    void minusSevenFindsAnyNumberOfElves() {
        Permanent nissa = addReadyNissa(player1, 7);
        harness.setLibrary(player1, List.of(
                new ElvishArchdruid(),
                new GrizzlyBears(),
                new ElvishArchdruid()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card ->
                card.hasType(CardType.CREATURE) && card.getSubtypes().contains(CardSubtype.ELF));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent ->
                permanent.getCard().getName().equals("Elvish Archdruid")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent nissa = new Permanent(new NissaRevane());
        nissa.setCounterCount(CounterType.LOYALTY, loyalty);
        nissa.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(nissa);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nissa;
    }

    private Card nissasChosen() {
        Card card = new Card();
        card.setName("Nissa's Chosen");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.ELF));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
