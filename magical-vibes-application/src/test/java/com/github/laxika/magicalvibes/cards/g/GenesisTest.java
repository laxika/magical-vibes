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
    @DisplayName("The graveyard upkeep ability triggers only during Genesis's owner's upkeep")
    void triggersOnlyDuringOwnersUpkeep() {
        harness.setGraveyard(player1, List.of(new Genesis(), new GiantWarthog()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
