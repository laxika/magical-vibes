package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Genesis.class, GiantWarthog.class, RiftstonePortal.class})
class GenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{G} returns the chosen creature card from the graveyard to hand")
    void payingUpkeepCostReturnsTargetCreatureToHand() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis, warthog));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Giant Warthog");
        harness.assertInGraveyard(player1, "Genesis");
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves the targeted creature card in the graveyard")
    void decliningUpkeepPaymentLeavesTargetInGraveyard() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis, warthog));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genesis");
        harness.assertInGraveyard(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("The graveyard upkeep ability triggers only during Genesis's owner's upkeep")
    void triggersOnlyDuringOwnersUpkeep() {
        harness.setGraveyard(player1, List.of(new Genesis(), new GiantWarthog()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability only offers creature cards as graveyard targets")
    void offersOnlyCreatureCardsAsTargets() {
        harness.setGraveyard(player1, List.of(new Genesis(), new RiftstonePortal()));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).hasSize(1);
        assertThat(choice.cards().getFirst().getName()).isEqualTo("Genesis");
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genesis");
        harness.assertInGraveyard(player1, "Riftstone Portal");
    }

    @Test
    @DisplayName("The ability does nothing if Genesis leaves the graveyard before resolution")
    void doesNotResolveIfSourceLeavesGraveyard() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis, warthog));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.setHand(player1, List.of(genesis));
        harness.setGraveyard(player1, List.of(warthog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Genesis");
        harness.assertInGraveyard(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Paying {2}{G} returns the chosen creature card from the graveyard to hand")
    void payingUpkeepCostReturnsTargetCreatureToHandJudReview() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        RiftstonePortal portal = new RiftstonePortal();
        harness.setGraveyard(player1, List.of(genesis, warthog, portal));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(genesis.getId(), warthog.getId());
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Giant Warthog");
        harness.assertInGraveyard(player1, "Genesis");
    }

    @Test
    @DisplayName("The ability does nothing if Genesis leaves the graveyard before resolution")
    void abilityDoesNothingIfGenesisLeavesGraveyardBeforeResolution() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis, warthog));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.setGraveyard(player1, List.of(warthog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Giant Warthog");
    }
    @Test
    @DisplayName("Genesis can return itself and consumes exactly the upkeep payment")
    void canReturnItself() {
        Genesis genesis = new Genesis();
        harness.setGraveyard(player1, List.of(genesis));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(genesis.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genesis");
        harness.assertNotInGraveyard(player1, "Genesis");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An absent graveyard target prevents resolution and the payment choice")
    void missingTargetPreventsPayment() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis, warthog));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));
        harness.setGraveyard(player1, List.of(genesis));
        harness.setHand(player1, List.of(warthog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Genesis");
        harness.assertInHand(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Genesis cannot target a creature in an opponent's graveyard")
    void targetsOnlyOwnersGraveyard() {
        Genesis genesis = new Genesis();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(genesis));
        harness.setGraveyard(player2, List.of(warthog));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(genesis.getId());
        harness.handleMultipleCardsChosen(player1, List.of(genesis.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genesis");
        harness.assertInGraveyard(player2, "Giant Warthog");
    }
}
