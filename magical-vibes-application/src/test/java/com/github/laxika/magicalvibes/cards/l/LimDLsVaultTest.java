package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CarrierPigeons;
import com.github.laxika.magicalvibes.cards.e.ErrandOfDuty;
import com.github.laxika.magicalvibes.cards.e.Exile;
import com.github.laxika.magicalvibes.cards.f.FontOfAgonies;
import com.github.laxika.magicalvibes.cards.i.Inheritance;
import com.github.laxika.magicalvibes.cards.i.IvoryGargoyle;
import com.github.laxika.magicalvibes.cards.j.JuniperOrderAdvocate;
import com.github.laxika.magicalvibes.cards.k.KjeldoranEscort;
import com.github.laxika.magicalvibes.cards.k.KjeldoranHomeGuard;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LimDLsVault.class, CarrierPigeons.class, ErrandOfDuty.class, Exile.class,
        Inheritance.class, IvoryGargoyle.class, JuniperOrderAdvocate.class,
        KjeldoranEscort.class, KjeldoranHomeGuard.class})
class LimDLsVaultTest extends BaseCardTest {

    private void castVault(List<Card> library) {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new LimDLsVault(), "{U}{B}");
        harness.passBothPriorities();
    }

    /** Eight distinguishable cards; index 0 is the top of the library. */
    private static List<Card> library8() {
        return new ArrayList<>(List.of(
                new CarrierPigeons(), new ErrandOfDuty(), new Exile(), new Inheritance(),
                new IvoryGargoyle(), new JuniperOrderAdvocate(), new KjeldoranEscort(),
                new KjeldoranHomeGuard()));
    }

    private void order(List<Integer> cardOrder) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(cardOrder));
    }

    @Test
    @DisplayName("Declining immediately costs no life and puts the five looked-at cards back on top in the chosen order")
    void declineImmediately() {
        List<Card> library = library8();
        List<Card> topFive = List.copyOf(library.subList(0, 5));
        castVault(library);

        // The five cards are held out of the library while the prompt is pending.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(4, 3, 2, 1, 0));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(8);
        assertThat(deck.subList(0, 5))
                .containsExactly(topFive.get(4), topFive.get(3), topFive.get(2), topFive.get(1), topFive.get(0));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting pays 1 life, bottoms the looked-at cards in the chosen order, and looks at five more")
    void acceptPaysLifeAndLooksAgain() {
        List<Card> library = library8();
        Card sixth = library.get(5);
        Card seventh = library.get(6);
        Card eighth = library.get(7);
        castVault(library);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // Bottom the first five in their original order, then five more are looked at: the three
        // cards left on top plus the first two just bottomed.
        order(List.of(0, 1, 2, 3, 4));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2, 3, 4));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(8);
        assertThat(deck.subList(0, 3)).containsExactly(sixth, seventh, eighth);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With five or fewer cards, paying to repeat looks at the same cards again")
    void smallLibraryCanBeRepeated() {
        List<Card> library = new ArrayList<>(List.of(new CarrierPigeons(), new ErrandOfDuty(), new Exile()));
        Card carrierPigeons = library.get(0);
        Card errandOfDuty = library.get(1);
        Card exile = library.get(2);
        castVault(library);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        order(List.of(0, 1, 2));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(carrierPigeons, errandOfDuty, exile);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Repeating several times pays 1 life each time")
    void repeatPaysOneLifeEachTime() {
        castVault(library8());

        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library smaller than five is looked at in full and comes back in the chosen order")
    void smallLibraryLooksAtEverything() {
        List<Card> library = new ArrayList<>(List.of(new CarrierPigeons(), new ErrandOfDuty(), new Exile()));
        Card carrierPigeons = library.get(0);
        Card errandOfDuty = library.get(1);
        Card exile = library.get(2);
        castVault(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(2, 0, 1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(exile, carrierPigeons, errandOfDuty);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library looks at nothing and prompts nothing")
    void emptyLibraryDoesNothing() {
        castVault(new ArrayList<>());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An order that is not a permutation of the looked-at cards is rejected")
    void rejectsInvalidOrder() {
        castVault(library8());

        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> order(List.of(0, 0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(FontOfAgonies.class)
    @DisplayName("Paying for a repeat triggers abilities that trigger when a player pays life")
    void repeatFiresLifePaymentTriggers() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        castVault(library8());

        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    @DisplayName("A player whose life total cannot change cannot accept a repeat")
    void cannotAcceptRepeatWhenLifeTotalCannotChange() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        castVault(library8());

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    @DisplayName("The repeat prompt marks payment unavailable when the life total cannot change")
    void repeatPromptDisablesUnavailablePayment() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        castVault(library8());

        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"INTERACTION_PROMPT\""))
                .anySatisfy(message -> assertThat(message).contains("\"canPay\":false"));
    }
}
