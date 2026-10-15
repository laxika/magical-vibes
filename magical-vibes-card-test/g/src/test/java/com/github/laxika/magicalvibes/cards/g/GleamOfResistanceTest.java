package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientZiggurat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleamOfResistance.class, GrizzlyBears.class, Plains.class, Forest.class, Island.class, AncientZiggurat.class})
class GleamOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts creatures you control +1/+2")
    void boostsOwnCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(opponentBear.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Untaps the creatures you control and boosts them in one resolution")
    void untapsAndBoostsInOneResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();

        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBear.tap();

        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

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

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    void doesNotUntapNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveBoost() {
        harness.castFromHand(player1, new GleamOfResistance(), "{4}{W}");
        harness.passBothPriorities();

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void landcyclingDiscardsBeforeResolutionAndDoesNotBoostOrUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Gleam of Resistance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void landcyclingExcludesNonbasicLands() {
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.setLibrary(player1, List.of(new AncientZiggurat(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Plains");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ancient Ziggurat");
    }

    @Test
    void landcyclingCanFailToFindEvenWithBasicLandAvailable() {
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landcyclingResolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new GleamOfResistance()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gleam of Resistance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

}
