package com.github.laxika.magicalvibes.cards.g;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GleamOfResistanceTest extends BaseCardTest {

    // ===== Spell: boost =====

    @Test
    @DisplayName("Resolving boosts creatures you control +1/+2")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent bear = firstCreature(player1);
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent opponentBear = firstCreature(player2);
        assertThat(opponentBear.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = firstCreature(player1);
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Spell: untap =====

    @Test
    @DisplayName("Untaps the creatures you control and boosts them in one resolution")
    void untapsAndBoostsInOneResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = firstCreature(player1);
        bear.tap();

        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentBear = firstCreature(player2);
        opponentBear.tap();

        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    // ===== Basic landcycling =====

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearches() {
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gleam of Resistance");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a basic land from the search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(),
                new GrizzlyBears()));
    }

    private Permanent firstCreature(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .findFirst()
                .orElseThrow();
    }
}
