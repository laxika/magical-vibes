package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FallOfTheTitans;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KozilekTheGreatDistortion.class, GrizzlyBears.class, Shock.class,
        FallOfTheTitans.class, MatterReshaper.class})
class KozilekTheGreatDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("When cast with fewer than seven cards in hand, draws up to seven")
    void castDrawsUpToSevenCards() {
        harness.setLibrary(player1, cards(5));
        KozilekTheGreatDistortion kozilek = new KozilekTheGreatDistortion();
        harness.setHand(player1, List.of(kozilek, new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kozilek, the Great Distortion");
    }

    @Test
    @DisplayName("Does not draw when casting leaves seven cards in hand")
    void castDoesNotDrawAtSevenCards() {
        KozilekTheGreatDistortion kozilek = new KozilekTheGreatDistortion();
        harness.setHand(player1, handWith(kozilek, 7));
        harness.setLibrary(player1, cards(1));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counters a spell when the discarded card has the same mana value")
    void countersSpellWithMatchingManaValue() {
        addReadyKozilek(player1);
        harness.setHand(player1, List.of(new Shock()));

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a card matching the target spell's mana value")
    void cannotActivateWithDifferentManaValue() {
        addReadyKozilek(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Draw trigger does nothing if the hand reaches seven before resolution")
    void drawTriggerRechecksHandSize() {
        harness.setHand(player1, List.of(new KozilekTheGreatDistortion()));
        harness.setLibrary(player1, cards(7));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.setHand(player1, cards(7));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Draw amount uses the hand size at resolution")
    void drawAmountUsesCurrentHandSize() {
        harness.setHand(player1, List.of(new KozilekTheGreatDistortion()));
        harness.setLibrary(player1, cards(7));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);

        harness.setHand(player1, cards(4));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Counts both X symbols in the target spell's mana value")
    void countersSpellWithTwoXSymbols() {
        addReadyKozilek(player1);
        harness.setHand(player1, List.of(new MatterReshaper()));
        FallOfTheTitans spell = new FallOfTheTitans();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.castInstantForX(player2, 0, 1, List.of(player1.getId()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Matter Reshaper");
        harness.assertInGraveyard(player2, "Fall of the Titans");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyKozilek(player1);
        harness.setHand(player1, List.of(new Shock()));
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKozilek(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new KozilekTheGreatDistortion());
    }

    @Test
    @DisplayName("Menace rejects one blocker")
    void menaceRejectsOneBlocker() {
        addReadyKozilek(player1);
        addCreatureReady(player2, new MatterReshaper());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void menaceAllowsTwoBlockers() {
        addReadyKozilek(player1);
        Permanent first = addCreatureReady(player2, new MatterReshaper());
        Permanent second = addCreatureReady(player2, new MatterReshaper());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private List<Card> handWith(KozilekTheGreatDistortion kozilek, int otherCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(kozilek);
        hand.addAll(cards(otherCards));
        return hand;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
