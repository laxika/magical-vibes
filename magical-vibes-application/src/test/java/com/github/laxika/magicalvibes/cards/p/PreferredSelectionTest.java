package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Foratog;
import com.github.laxika.magicalvibes.cards.n.NettletoothDjinn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({PreferredSelection.class, Foratog.class, NettletoothDjinn.class, Forest.class})
class PreferredSelectionTest extends BaseCardTest {

    private Card top;
    private Card second;
    private Permanent preferredSelection;

    private void setup() {
        preferredSelection = harness.addToBattlefieldAndReturn(player1, new PreferredSelection());
        top = new Foratog();
        second = new NettletoothDjinn();
        harness.setLibrary(player1, List.of(top, second, new Forest(), new Forest()));
    }

    private void triggerUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    @Test
    void upkeepOffersTheSacrificeAndPayChoice() {
        setup();

        triggerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void payingSacrificesTheEnchantmentAndPutsAChosenCardIntoHand() {
        setup();

        triggerUpkeep();
        // Added after the step advance — mana pools empty between steps.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(preferredSelection);
    }

    @Test
    void decliningBottomsOneOfTheTwoCardsAndKeepsTheEnchantment() {
        setup();

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, false);
        // The pick names the card that stays on top; the other one goes to the bottom.
        harness.handleCardChosen(player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(second);
        assertThat(deck.getLast()).isSameAs(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(preferredSelection);
    }

    @Test
    void withoutManaTheDeclineBranchStillBottomsACard() {
        setup();

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(top);
        assertThat(deck.getLast()).isSameAs(second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(preferredSelection);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        setup();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(top);
        assertThat(deck.get(1)).isSameAs(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(preferredSelection);
    }

    @Test
    void cannotUseThePaymentOptionAfterEnchantmentLeavesTheBattlefield() {
        setup();

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(preferredSelection);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, second);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(second);
        assertThat(deck.getLast()).isSameAs(top);
    }
}
