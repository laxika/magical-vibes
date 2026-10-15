package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.n.NivMizzetParun;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeviousCoverUp.class, OrneryGoblin.class, NivMizzetParun.class, CosisTrickster.class})
class DeviousCoverUpTest extends BaseCardTest {

    @Test
    @DisplayName("Counters and exiles a spell, then shuffles up to four chosen cards")
    void countersExilesAndShufflesFourCards() {
        Card target = new OrneryGoblin();
        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        List<Card> buried = List.of(
                new DeviousCoverUp(), new OrneryGoblin(), new DeviousCoverUp(),
                new OrneryGoblin(), new DeviousCoverUp());
        harness.setGraveyard(player2, buried);
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, target.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                buried.stream().map(Card::getId).toList());

        harness.handleMultipleCardsChosen(player2, buried.subList(0, 4).stream().map(Card::getId).toList());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player1, "Ornery Goblin");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .contains(buried.get(4))
                .doesNotContainAnyElementsOf(buried.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 4);
        assertThat(gd.playerDecks.get(player2.getId())).containsAll(buried.subList(0, 4));
    }

    @Test
    @DisplayName("Choosing zero graveyard targets still exiles the countered spell")
    void choosingZeroGraveyardTargetsStillExilesSpell() {
        Card target = new OrneryGoblin();
        Card buried = new DeviousCoverUp();
        harness.setGraveyard(player2, List.of(buried));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(buried);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
    }

    @Test
    @DisplayName("With no graveyard cards, the counter target is still exiled")
    void countersAndExilesWithEmptyGraveyard() {
        Card target = new OrneryGoblin();
        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertInGraveyard(player2, "Devious Cover-Up");
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a spell on the stack")
    void cannotTargetPermanent() {
        OrneryGoblin target = new OrneryGoblin();
        harness.addToBattlefield(player1, target);
        UUID permanentId = harness.getPermanentId(player1, "Ornery Goblin");

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The optional shuffle can be declined at resolution after choosing targets")
    void canDeclineShuffleAfterChoosingTargets() {
        Card target = new OrneryGoblin();
        Card buried = new OrneryGoblin();
        harness.setGraveyard(player2, List.of(buried));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(buried.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(buried);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
        harness.assertInGraveyard(player2, "Devious Cover-Up");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @CardUsed(CosisTrickster.class)
    @DisplayName("Choosing zero graveyard targets still allows a library shuffle")
    void canShuffleWithZeroGraveyardTargets(boolean hasGraveyardCards) {
        harness.addToBattlefield(player1, new CosisTrickster());
        Card target = new OrneryGoblin();
        harness.setGraveyard(player2, hasGraveyardCards ? List.of(new OrneryGoblin()) : List.of());
        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        if (hasGraveyardCards) {
            harness.handleMultipleCardsChosen(player2, List.of());
        }
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard()).isInstanceOf(CosisTrickster.class));
    }

    @Test
    @DisplayName("An uncounterable spell stays on the stack while graveyard cards are shuffled")
    void shufflesCardsWhenSpellCannotBeCountered() {
        Card target = new NivMizzetParun();
        Card buried = new OrneryGoblin();
        harness.setGraveyard(player2, List.of(buried));
        harness.castFromHand(player1, target, "{U}{U}{U}{R}{R}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(buried.getId()));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(target);
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getId()).isEqualTo(target.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).contains(buried);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(buried);
    }

    @Test
    @DisplayName("The shuffle still resolves when the target spell leaves the stack")
    void shufflesCardsWhenSpellTargetBecomesIllegal() {
        Card target = new OrneryGoblin();
        Card buried = new OrneryGoblin();
        harness.setGraveyard(player2, List.of(buried));
        harness.castFromHand(player1, target, "{1}{R}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(buried.getId()));

        harness.setHand(player1, List.of(new DeviousCoverUp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.playerDecks.get(player2.getId())).contains(buried);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(buried);
    }
}
