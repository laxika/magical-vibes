package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalinLoremaster.class, FountainOfYouth.class, GrizzlyBears.class})
class BalinLoremasterTest extends BaseCardTest {

    @Test
    void mayDiscardHandAndDrawThatManyOnOwnEntryWithoutEnduringStoryDamage() {
        setDeck(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        chooseBalinAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void decliningTheAbilityLeavesTheHandUntouched() {
        harness.addToBattlefield(player1, new BalinLoremaster());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.enterBattlefieldAndReturn(player1, dwarfCard());
        chooseBalinAbility(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enduringStoryMakesTheAbilityDealDamageEqualToDiscardedCards() {
        setDeck(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.enterBattlefieldAndReturn(player1, new BalinLoremaster());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        chooseBalinAbility(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void chooseBalinAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private static Card dwarfCard() {
        Card card = new Card();
        card.setName("Dwarf");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.DWARF));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
