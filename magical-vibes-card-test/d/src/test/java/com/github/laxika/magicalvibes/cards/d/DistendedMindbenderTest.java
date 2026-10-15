package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CivicWayfinder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThievingMagpie;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistendedMindbender.class, AirElemental.class, CivicWayfinder.class,
        Forest.class, GrizzlyBears.class, Shock.class, ThievingMagpie.class})
class DistendedMindbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger: discard a nonland MV≤3 and a MV≥4 from the opponent's hand")
    void discardsBothBands() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), // MV 2 nonland
                new Forest(),       // land — not choosable for first band
                new AirElemental()  // MV 5
        )));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve cast trigger → hand choice

        PendingInteraction.RevealedHandChoice first =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(first).isNotNull();
        assertThat(first.validIndices()).containsExactly(0); // only Bears for MV≤3 nonland
        harness.handleCardChosen(player1, 0);

        PendingInteraction.RevealedHandChoice second =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(second).isNotNull();
        // Hand after removing Bears: Forest (0), Air Elemental (1)
        assertThat(second.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Air Elemental");
    }

    @Test
    @DisplayName("Only one band present: discard just that card")
    void discardsOnlyMatchingBand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Shock(), new Forest())));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0); // Shock only; no MV≥4 follow-up
        assertThat(choice.followUpFilter()).isNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Only MV≥4 band present: skip first band and discard the high card")
    void skipsEmptyFirstBand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new AirElemental())));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1); // Air Elemental only
        assertThat(choice.followUpFilter()).isNull();
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player2, new ArrayList<>(List.of(new Shock())));
        harness.setHand(player1, List.of(new DistendedMindbender()));
        // Emerge {5}{B}{B} reduced by 2 → {3}{B}{B}
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0); // discard Shock
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Distended Mindbender");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast trigger cannot target the controller")
    void castTriggerCannotTargetSelf() {
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void includesManaValuesThreeAndFourInTheirRespectiveBands() {
        harness.setHand(player2, List.of(new CivicWayfinder(), new ThievingMagpie(), new Forest()));
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        harness.assertNotInGraveyard(player2, "Civic Wayfinder");
        harness.assertNotInGraveyard(player2, "Thieving Magpie");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Civic Wayfinder");
        harness.assertInGraveyard(player2, "Thieving Magpie");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Forest");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Distended Mindbender");
    }

    @Test
    void handContainingOnlyLandsDoesNotRequireAChoice() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Distended Mindbender");
    }

    @Test
    void emptyHandDoesNotPreventCreatureResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Distended Mindbender");
    }

    @Test
    void enteringWithoutBeingCastDoesNotDiscard() {
        harness.setHand(player2, List.of(new Shock(), new AirElemental()));

        harness.enterBattlefieldAndReturn(player1, new DistendedMindbender());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void emergeReductionCannotRemoveColoredMana() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new DistendedMindbender()).getId();
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Distended Mindbender");
        harness.assertOnBattlefield(player1, "Distended Mindbender");
    }

    @Test
    void emergeReductionIsCappedAtGenericCost() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new DistendedMindbender()).getId();
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Distended Mindbender");
        harness.assertOnBattlefield(player1, "Distended Mindbender");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void sacrificingFaceDownCreatureDoesNotReduceEmergeCost() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrifice.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Distended Mindbender");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
