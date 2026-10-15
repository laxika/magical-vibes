package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetherTraitor.class, AshcoatBear.class})
class NetherTraitorTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B} returns Nether Traitor from the graveyard to the battlefield")
    void payingBlackReturnsNetherTraitor() {
        Card traitor = new NetherTraitor();
        harness.setGraveyard(player1, List.of(traitor));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returnedTraitor = findPermanent(player1, "Nether Traitor");
        assertThat(returnedTraitor.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("Declining the payment leaves Nether Traitor in the graveyard")
    void decliningKeepsNetherTraitorInGraveyard() {
        Card traitor = new NetherTraitor();
        harness.setGraveyard(player1, List.of(traitor));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nether Traitor");
        harness.assertNotOnBattlefield(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("A creature put into an opponent's graveyard does not trigger Nether Traitor")
    void opponentCreatureDoesNotTrigger() {
        Card traitor = new NetherTraitor();
        harness.setGraveyard(player1, List.of(traitor));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("Accepting without {B} does not return Nether Traitor")
    void cannotReturnWithoutBlackMana() {
        Card traitor = new NetherTraitor();
        harness.setGraveyard(player1, List.of(traitor));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Nether Traitor");
        harness.assertNotOnBattlefield(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("Nether Traitor does not trigger when it dies with another creature")
    void simultaneousDeathDoesNotTriggerItself() {
        Permanent traitor = harness.addToBattlefieldAndReturn(player1, new NetherTraitor());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addMana(player1, ManaColor.BLACK, 1);
        traitor.setMarkedDamage(1);
        bear.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("A creature you do not control still triggers it when it enters your graveyard")
    void creatureOwnedByControllerButControlledByOpponentTriggers() {
        Card traitor = new NetherTraitor();
        harness.setGraveyard(player1, List.of(traitor));
        Card bearCard = new AshcoatBear();
        bearCard.setOwnerId(player1.getId());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, bearCard);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Nether Traitor");
        harness.assertNotInGraveyard(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("An old trigger cannot return Nether Traitor after it returns and dies again")
    void oldTriggerCannotReturnNewGraveyardObject() {
        harness.setGraveyard(player1, List.of(new NetherTraitor()));
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addMana(player1, ManaColor.BLACK, 2);
        firstBear.setMarkedDamage(2);
        secondBear.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Nether Traitor");

        Permanent returnedTraitor = findPermanent(player1, "Nether Traitor");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returnedTraitor));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Nether Traitor");
        harness.assertNotOnBattlefield(player1, "Nether Traitor");
    }

    @Test
    @DisplayName("Nether Traitor on the battlefield does not trigger when another creature dies")
    void abilityOnlyFunctionsInGraveyard() {
        harness.addToBattlefield(player1, new NetherTraitor());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nether Traitor");
    }
}
