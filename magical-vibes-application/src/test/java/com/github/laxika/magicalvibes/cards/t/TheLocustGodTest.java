package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheLocustGodTest extends BaseCardTest {

    private long insectTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Insect"))
                .count();
    }

    private Permanent findInsect(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Insect"))
                .findFirst()
                .orElseThrow();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Draw step draw creates a 1/1 blue-red Insect with flying and haste")
    void drawCreatesInsectToken() {
        harness.addToBattlefield(player1, new TheLocustGod());
        setDeck(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(insectTokenCount(player1)).isEqualTo(1);
        Permanent insect = findInsect(player1);
        assertThat(insect.getCard().getPower()).isEqualTo(1);
        assertThat(insect.getCard().getToughness()).isEqualTo(1);
        assertThat(insect.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(insect.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(insect.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Drawing multiple cards creates one Insect per card drawn")
    void createsOneInsectPerCardDrawn() {
        harness.addToBattlefield(player1, new TheLocustGod());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel (draws 2)
        harness.passBothPriorities(); // first Insect trigger
        harness.passBothPriorities(); // second Insect trigger

        assertThat(insectTokenCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent drawing does not create Insect tokens")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new TheLocustGod());
        setDeck(player2, List.of(new GrizzlyBears()));

        advanceToDraw(player2);

        assertThat(insectTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Activated ability draws then discards, and the draw creates an Insect")
    void lootAbilityDrawsDiscardsAndCreatesInsect() {
        harness.addToBattlefield(player1, new TheLocustGod());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve loot: draw + discard prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0); // discard

        harness.passBothPriorities(); // resolve Insect trigger from the draw

        assertThat(insectTokenCount(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("When The Locust God dies, it returns to hand at the beginning of the next end step")
    void diesReturnsToHandAtNextEndStep() {
        Permanent locust = harness.addToBattlefieldAndReturn(player1, new TheLocustGod());
        Card locustCard = locust.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Locust dies, death trigger on stack
        harness.passBothPriorities(); // resolve death trigger — register delayed return

        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(locustCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(locustCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(locustCard.getId()));
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
