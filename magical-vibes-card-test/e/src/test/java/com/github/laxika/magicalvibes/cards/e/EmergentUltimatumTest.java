package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdaptiveShimmerer;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.n.Neutralize;
import com.github.laxika.magicalvibes.cards.p.PhaseDolphin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergentUltimatum.class, EerieUltimatum.class, GrizzlyBears.class,
        LlanowarElves.class, RagingGoblin.class, AdaptiveShimmerer.class,
        AlmightyBrushwagg.class, CatharticReunion.class, Neutralize.class, PhaseDolphin.class})
class EmergentUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three differently named monocolored cards and offers the rest to cast")
    void searchesChoosesShufflesAndOffersRemainingCards() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card goblin = new RagingGoblin();
        Card multicolored = new EerieUltimatum();
        EmergentUltimatum spell = new EmergentUltimatum();

        harness.setLibrary(player1, List.of(bears, multicolored, elves, goblin));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();

        assertThat(offeredCards()).containsExactlyInAnyOrder(bears, elves, goblin);

        find(bears, elves, goblin);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(elves.getId(), goblin.getId());

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(goblin.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(multicolored.getId(), bears.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(elves.getId(), spell.getId());
    }

    @Test
    @DisplayName("A card with the name of an already found card is not offered again")
    void offersOnlyDifferentNames() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, elves));
        harness.castFromHand(player1, new EmergentUltimatum(), "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, offeredCards().indexOf(first));

        assertThat(offeredCards()).containsExactly(elves);
    }

    @Test
    @DisplayName("Can find zero cards even when monocolored cards are available")
    void canFindZeroCards() {
        Card brushwagg = new AlmightyBrushwagg();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(brushwagg));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(brushwagg);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless and multicolored cards are not eligible search results")
    void excludesColorlessAndMulticoloredCards() {
        Card colorless = new AdaptiveShimmerer();
        Card multicolored = new EerieUltimatum();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(colorless, multicolored));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(colorless, multicolored);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Finding only one card shuffles it into the library unasked, without a free cast")
    void findingOneCardOffersNoSpells() {
        Card brushwagg = new AlmightyBrushwagg();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(brushwagg));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();

        find(brushwagg);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(brushwagg);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may decline to cast the remaining card")
    void mayDeclineAllFreeCasts() {
        Card brushwagg = new AlmightyBrushwagg();
        Card dolphin = new PhaseDolphin();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(brushwagg, dolphin));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();
        find(brushwagg, dolphin);
        harness.handleMultipleCardsChosen(player2, List.of(brushwagg.getId()));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(brushwagg);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(dolphin, spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both remaining cards can be cast in the controller's chosen order")
    void castsBothCardsInChosenOrder() {
        Card brushwagg = new AlmightyBrushwagg();
        Card dolphin = new PhaseDolphin();
        Card reunion = new CatharticReunion();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(brushwagg, dolphin, reunion));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();
        find(brushwagg, dolphin, reunion);
        harness.handleMultipleCardsChosen(player2, List.of(reunion.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(dolphin.getId(), brushwagg.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .containsExactly(dolphin.getId(), brushwagg.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(dolphin.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("A free spell with a payable mandatory additional cost must offer cost payment")
    void offersPaymentForMandatoryAdditionalCost() {
        Card reunion = new CatharticReunion();
        Card brushwagg = new AlmightyBrushwagg();
        Card firstDiscard = new PhaseDolphin();
        Card secondDiscard = new AdaptiveShimmerer();
        harness.setLibrary(player1, List.of(reunion, brushwagg));
        harness.setHand(player1, List.of(new EmergentUltimatum(), firstDiscard, secondDiscard));
        addEmergentUltimatumMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        find(reunion, brushwagg);
        harness.handleMultipleCardsChosen(player2, List.of(brushwagg.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(reunion.getId()));

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);
    }

    @Test
    @DisplayName("A free counterspell can target Emergent Ultimatum while it is resolving")
    void canTargetResolvingUltimatum() {
        Card neutralize = new Neutralize();
        Card brushwagg = new AlmightyBrushwagg();
        EmergentUltimatum spell = new EmergentUltimatum();
        harness.setLibrary(player1, List.of(neutralize, brushwagg));
        harness.castFromHand(player1, spell, "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();
        find(neutralize, brushwagg);
        harness.handleMultipleCardsChosen(player2, List.of(brushwagg.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(neutralize.getId()));

        PendingInteraction.PermanentChoice targets =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targets).isNotNull();
        assertThat(targets.validPermanentIds()).contains(spell.getId());
        harness.handlePermanentChosen(player1, spell.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell).doesNotContain(neutralize);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Neutralize");
    }

    @Test
    @DisplayName("The opponent chooses which found card is shuffled away")
    void opponentChoosesTheShuffledCard() {
        Card brushwagg = new AlmightyBrushwagg();
        Card dolphin = new PhaseDolphin();
        harness.setLibrary(player1, List.of(brushwagg, dolphin));
        harness.castFromHand(player1, new EmergentUltimatum(), "{B}{B}{G}{G}{G}{U}{U}");
        harness.passBothPriorities();
        find(brushwagg, dolphin);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(dolphin.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> offeredCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
    }

    private void find(Card... cards) {
        for (Card card : cards) {
            harness.handleCardChosen(player1, offeredCards().indexOf(card));
        }
        if (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) != null) {
            harness.handleCardChosen(player1, -1);
        }
    }

    private void addEmergentUltimatumMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
