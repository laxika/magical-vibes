package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BygoneColossus;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RovingActuator.class, BygoneColossus.class, CounselOfTheSoratami.class,
        Forest.class, GrizzlyBears.class, Pyroclasm.class, Shock.class})
class RovingActuatorTest extends BaseCardTest {

    @Test
    void voidEtbCopiesAndCastsTargetedSpell() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card shock = new Shock();
        Card invalidManaValue = new CounselOfTheSoratami();
        Card invalidType = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, invalidManaValue, invalidType));
        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(invalidManaValue, invalidType);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void voidEtbMayBeDeclinedWithoutChoosingATarget() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void voidEtbWithNoValidTargetStillResolves() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));

        Card invalidTarget = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(invalidTarget));
        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalidTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void etbDoesNotTriggerWithoutVoid() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void opponentNonlandDepartureEnablesVoidButOnlyOwnGraveyardCanBeTargeted() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));
        Card ownShock = new Shock();
        Card opposingShock = new Shock();
        harness.setGraveyard(player1, List.of(ownShock));
        harness.setGraveyard(player2, List.of(opposingShock));

        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownShock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownShock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingShock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownShock);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void warpedSpellEnablesVoidBeforeAnyPermanentLeaves() {
        harness.setHand(player1, List.of(new BygoneColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.nonlandPermanentLeftBattlefieldThisTurn).isFalse();

        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
    }

    @Test
    void copiesManaValueTwoSorceryWithoutPayingMana() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card pyroclasm = new Pyroclasm();
        harness.setGraveyard(player1, List.of(pyroclasm));

        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(pyroclasm.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Roving Actuator");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(pyroclasm);
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionPreventsCopyOffer() {
        Permanent leavingPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingPermanent));
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(shock));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        harness.assertLife(player2, 20);
    }

    @Test
    void landDepartureDoesNotEnableVoid() {
        Permanent leavingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, leavingLand));
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.castFromHand(player1, new RovingActuator(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
