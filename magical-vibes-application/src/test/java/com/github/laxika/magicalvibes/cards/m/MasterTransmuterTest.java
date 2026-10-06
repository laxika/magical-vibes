package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CourtHomunculus;
import com.github.laxika.magicalvibes.cards.f.FrontlineSage;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterTransmuter.class, CourtHomunculus.class, FrontlineSage.class})
class MasterTransmuterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen artifact and puts an artifact from hand onto the battlefield")
    void returnsArtifactAndPutsArtifactFromHand() {
        addCreatureReady(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new CourtHomunculus());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID homunculusId = findPermanent(player1, "Court Homunculus").getId();

        harness.activateAbility(player1, 0, null, null);
        // Two artifacts you control (Transmuter + Court Homunculus) -> choose which to return.
        harness.handlePermanentChosen(player1, homunculusId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Put the returned Court Homunculus back onto the battlefield.
        harness.handleCardChosen(player1, handIndexOf(player1, "Court Homunculus"));

        Permanent homunculus = findPermanent(player1, "Court Homunculus");
        assertThat(homunculus).isNotNull();
        assertThat(homunculus.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Court Homunculus");
    }

    @Test
    @DisplayName("Can return itself to hand to pay the cost")
    void canReturnItselfToPayTheCost() {
        addCreatureReady(player1, new MasterTransmuter());
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Transmuter is the only artifact you control, so it is returned automatically.
        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player1, "Master Transmuter");
        harness.assertNotOnBattlefield(player1, "Master Transmuter");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Only artifact cards in hand may be put onto the battlefield")
    void onlyArtifactCardsAreOffered() {
        addCreatureReady(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new CourtHomunculus());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FrontlineSage()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Court Homunculus").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        List<Integer> validIndices = ((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices();
        // The returned Court Homunculus is a valid choice; the Frontline Sage (a nonartifact creature) is not.
        assertThat(validIndices).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(validIndices.getFirst()).getName())
                .isEqualTo("Court Homunculus");
    }

    @Test
    @DisplayName("Declining the may leaves the returned artifact in hand")
    void decliningLeavesArtifactInHand() {
        addCreatureReady(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new CourtHomunculus());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Court Homunculus").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Court Homunculus");
        harness.assertNotOnBattlefield(player1, "Court Homunculus");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new CourtHomunculus());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can put itself back onto the battlefield as a new untapped creature")
    void canPutItselfBackOntoBattlefield() {
        Permanent original = addCreatureReady(player1, new MasterTransmuter());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Master Transmuter");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, handIndexOf(player1, "Master Transmuter"));

        Permanent returned = findPermanent(player1, "Master Transmuter");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInHand(player1, "Master Transmuter");
    }

    @Test
    @DisplayName("Returns a borrowed artifact to its owner and puts a different artifact from hand")
    void returnsBorrowedArtifactToOwner() {
        addCreatureReady(player1, new MasterTransmuter());
        CourtHomunculus borrowed = new CourtHomunculus();
        borrowed.setOwnerId(player2.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, borrowed);
        permanent.tap();
        harness.setHand(player1, List.of(new CourtHomunculus()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, permanent.getId());

        harness.assertInHand(player2, "Court Homunculus");
        harness.assertNotOnBattlefield(player1, "Court Homunculus");
        assertThat(findPermanent(player1, "Master Transmuter").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, handIndexOf(player1, "Court Homunculus"));

        harness.assertOnBattlefield(player1, "Court Homunculus");
        harness.assertNotInHand(player1, "Court Homunculus");
        harness.assertInHand(player2, "Court Homunculus");
    }

    @Test
    @DisplayName("Resolves without putting anything onto the battlefield when no artifacts remain in hand")
    void resolvesWithNoArtifactInHand() {
        addCreatureReady(player1, new MasterTransmuter());
        CourtHomunculus borrowed = new CourtHomunculus();
        borrowed.setOwnerId(player2.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, borrowed);
        harness.setHand(player1, List.of(new FrontlineSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, permanent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frontline Sage");
        harness.assertInHand(player2, "Court Homunculus");
        harness.assertNotOnBattlefield(player1, "Court Homunculus");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new MasterTransmuter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Master Transmuter");
        assertThat(findPermanent(player1, "Master Transmuter").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private int handIndexOf(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        List<com.github.laxika.magicalvibes.model.Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException(cardName + " not in hand");
    }
}
