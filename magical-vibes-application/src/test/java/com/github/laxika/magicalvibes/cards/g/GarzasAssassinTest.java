package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.cards.m.MartyrOfBones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarzasAssassin.class, BorealDruid.class, BorealShelf.class, MartyrOfBones.class})
class GarzasAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys a target nonblack creature")
    void sacrificesAndDestroysNonblackCreature() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Garza's Assassin");
        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MartyrOfBones());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");

        harness.assertOnBattlefield(player1, "Garza's Assassin");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealShelf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");

        harness.assertOnBattlefield(player1, "Garza's Assassin");
        harness.assertOnBattlefield(player2, "Boreal Shelf");
    }

    @Test
    @DisplayName("Destroying a creature respects its regeneration shield")
    void destructionRespectsRegenerationShield() {
        addReadyAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Boreal Druid");
        harness.assertNotInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Recover returns it to hand after paying half life")
    void recoverReturnsItToHandWhenPaid() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(assassin);
    }

    @Test
    @DisplayName("Recover exiles it when declined")
    void recoverExilesItWhenDeclined() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(assassin);
    }

    @Test
    @DisplayName("Recover rounds half-life payment up on odd life totals")
    void recoverRoundsHalfLifePaymentUp() {
        Card assassin = new GarzasAssassin();
        harness.setLife(player1, 21);
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
    }

    @Test
    @DisplayName("Recover does not trigger when an opponent's creature dies")
    void recoverDoesNotTriggerForOpponentCreature() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(assassin);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(assassin);
    }

    @Test
    @DisplayName("Recover does not trigger when Garza's Assassin itself dies")
    void recoverDoesNotTriggerForItsOwnDeath() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, assassin));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Garza's Assassin");
    }

    @Test
    @DisplayName("Can sacrifice itself while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());
        assassin.setTapped(true);
        assassin.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Garza's Assassin");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Recover triggers when its activated ability destroys its owner's creature")
    void recoversAfterDestroyingOwnCreature() {
        addReadyAssassin();
        Card assassin = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
        harness.assertInGraveyard(player1, "Boreal Druid");
        harness.assertNotInGraveyard(player1, "Garza's Assassin");
    }

    @Test
    @DisplayName("Recover uses the life total at resolution rather than at triggering")
    void recoverUsesLifeAtResolution() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.setLife(player1, 13);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 6);
        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
    }

    @Test
    @DisplayName("Recover does not trigger when it dies simultaneously with another creature")
    void recoverDoesNotTriggerForSimultaneousDeath() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        assassin.setMarkedDamage(2);
        druid.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Garza's Assassin");
        harness.assertInGraveyard(player1, "Boreal Druid");
    }

    @Test
    @DisplayName("An old recover trigger cannot exile the Assassin after it leaves and reenters the graveyard")
    void oldRecoverTriggerCannotExileNewGraveyardObject() {
        Card assassin = new GarzasAssassin();
        harness.setGraveyard(player1, List.of(assassin));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, assassin.getId()));
        Permanent returnedAssassin = harness.addToBattlefieldAndReturn(player1, assassin);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returnedAssassin));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(assassin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(assassin);
        harness.assertLife(player1, 20);
    }

    private void addReadyAssassin() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new GarzasAssassin());
        assassin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
