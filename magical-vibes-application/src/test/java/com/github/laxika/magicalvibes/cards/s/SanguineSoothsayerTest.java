package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanguineSoothsayer.class, SanguineBond.class})
class SanguineSoothsayerTest extends BaseCardTest {

    @Test
    void attackConjuresSanguineBondIntoTopFifteenCards() {
        harness.setLibrary(player1, cards(15));
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(16);
        assertThat(library.subList(0, 15)).filteredOn(Card::getName, "Sanguine Bond").hasSize(1);
        assertThat(library.subList(15, 16)).allMatch(card -> !"Sanguine Bond".equals(card.getName()));
        assertThat(library.stream()
                .filter(card -> "Sanguine Bond".equals(card.getName()))
                .allMatch(card -> player1.getId().equals(card.getOwnerId())))
                .isTrue();
    }

    @Test
    void conjuredBondCanBeCastForFreeAndDrawsOnEntry() {
        harness.setLibrary(player1, cards(15));
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Card conjuredBond = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> "Sanguine Bond".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        harness.setLibrary(player1, List.of(new SanguineSoothsayer()));
        harness.setHand(player1, List.of(conjuredBond));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sanguine Bond")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Sanguine Soothsayer");
    }

    @Test
    void conjuringPreservesTheOrderOfExistingLibraryCards() {
        List<Card> originalCards = cards(20);
        harness.setLibrary(player1, originalCards);
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> !"Sanguine Bond".equals(card.getName()))
                .containsExactlyElementsOf(originalCards);
    }

    @Test
    void attackConjuresIntoAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Sanguine Bond");
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    void attackConjuresIntoAShortLibrary() {
        List<Card> originalCards = cards(3);
        harness.setLibrary(player1, originalCards);
        addCreatureReady(player1, new SanguineSoothsayer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).containsAll(originalCards);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(Card::getName, "Sanguine Bond").hasSize(1);
    }

    @Test
    void attackUsesTheControllersLibraryRatherThanTheOwnersLibrary() {
        List<Card> ownerLibrary = cards(3);
        harness.setLibrary(player1, ownerLibrary);
        harness.setLibrary(player2, cards(3));
        Card soothsayer = new SanguineSoothsayer();
        soothsayer.setOwnerId(player1.getId());
        addCreatureReady(player2, soothsayer);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownerLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4)
                .filteredOn(Card::getName, "Sanguine Bond")
                .singleElement()
                .satisfies(card -> assertThat(card.getOwnerId()).isEqualTo(player2.getId()));
    }

    @Test
    void perpetualAbilitiesSurviveReturningToHandAndDoNotApplyToOtherBonds() {
        harness.setLibrary(player1, cards(15));
        addCreatureReady(player1, new SanguineSoothsayer());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Card conjuredBond = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> "Sanguine Bond".equals(card.getName()))
                .findFirst().orElseThrow();
        harness.setLibrary(player1, cards(3));
        harness.setHand(player1, List.of(conjuredBond));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.setHand(player1, List.of(new SanguineBond()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, findPermanent(player1, "Sanguine Bond")));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sanguine Bond")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new SanguineSoothsayer());
        }
        return cards;
    }
}
