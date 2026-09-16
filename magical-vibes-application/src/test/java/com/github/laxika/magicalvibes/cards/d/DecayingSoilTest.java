package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecayingSoil.class, GrizzlyBears.class, Shock.class})
class DecayingSoilTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card from its controller's graveyard at upkeep")
    void exilesOneCardAtUpkeep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new DecayingSoil());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Threshold lets its controller pay to return an own nontoken creature to hand")
    void thresholdReturnsDyingCreatureToHandAfterPayment() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        var dyingCard = new GrizzlyBears();
        harness.addToBattlefield(player1, dyingCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        var bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve Shock and put the death trigger on the stack
        harness.passBothPriorities(); // resolve the death trigger and show the may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Threshold ability is absent below seven graveyard cards")
    void noReturnTriggerBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 5));
        harness.addToBattlefield(player1, new DecayingSoil());
        var dyingCard = new GrizzlyBears();
        harness.addToBattlefield(player1, dyingCard);

        var bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Threshold triggers for an owned creature even when an opponent controls it")
    void thresholdTriggersForOwnedCreatureControlledByOpponent() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        var dyingCard = new GrizzlyBears();
        dyingCard.setOwnerId(player1.getId());
        Permanent dyingPermanent = harness.addToBattlefieldAndReturn(player2, dyingCard);
        gd.stolenCreatures.put(dyingPermanent.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        putIntoGraveyard(dyingPermanent);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Threshold does not trigger for an opponent-owned creature you control")
    void thresholdDoesNotTriggerForOpponentOwnedCreatureControlledByYou() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        var dyingCard = new GrizzlyBears();
        dyingCard.setOwnerId(player2.getId());
        Permanent dyingPermanent = harness.addToBattlefieldAndReturn(player1, dyingCard);
        gd.stolenCreatures.put(dyingPermanent.getId(), player2.getId());

        putIntoGraveyard(dyingPermanent);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Threshold ignores a token creature put into its controller's graveyard")
    void thresholdIgnoresTokenCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        var tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        putIntoGraveyard(token);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(tokenCard.getId()));
    }

    @Test
    @DisplayName("Threshold does not trigger for a nontoken noncreature permanent")
    void thresholdIgnoresNoncreaturePermanent() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        Permanent decayingSoil = harness.addToBattlefieldAndReturn(player1, new DecayingSoil());

        putIntoGraveyard(decayingSoil);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(decayingSoil.getCard().getId()));
    }

    @Test
    @DisplayName("Declining the threshold payment leaves the creature in the graveyard")
    void decliningThresholdPaymentLeavesCreatureInGraveyard() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new DecayingSoil());
        var dyingCard = new GrizzlyBears();
        Permanent dyingPermanent = harness.addToBattlefieldAndReturn(player1, dyingCard);

        putIntoGraveyard(dyingPermanent);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
