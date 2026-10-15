package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CultivatorDrone;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GravityNegator.class, CultivatorDrone.class})
class GravityNegatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets another creature, not Gravity Negator")
    void attackTargetsAnotherCreature() {
        Permanent source = addCreatureReady(player1, new GravityNegator());
        Permanent ownDrone = addCreatureReady(player1, new CultivatorDrone());
        Permanent opponentDrone = addCreatureReady(player2, new CultivatorDrone());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownDrone.getId(), opponentDrone.getId())
                .doesNotContain(source.getId());
    }

    @Test
    @DisplayName("Paying {C} gives the target creature flying until end of turn")
    void payingColorlessManaGrantsFlying() {
        addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player2, new CultivatorDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining the payment does not give the target creature flying")
    void decliningPaymentDoesNotGrantFlying() {
        addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player2, new CultivatorDrone());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Colored mana cannot pay the colorless attack payment")
    void coloredManaCannotPayForFlying() {
        addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player2, new CultivatorDrone());
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack ability can grant flying to another creature you control")
    void grantsFlyingToOwnCreature() {
        addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player1, new CultivatorDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("With no other creature, the attack ability has no legal target")
    void noOtherCreatureDoesNotOfferPayment() {
        addCreatureReady(player1, new GravityNegator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("The attack ability resolves even if Gravity Negator leaves the battlefield")
    void sourceLeavingDoesNotPreventFlying() {
        Permanent source = addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player2, new CultivatorDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("If the target leaves before resolution, the ability does not offer payment")
    void removedTargetDoesNotOfferPayment() {
        addCreatureReady(player1, new GravityNegator());
        Permanent drone = addCreatureReady(player2, new CultivatorDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drone.getId());
        gd.playerBattlefields.get(player2.getId()).remove(drone);
        gd.playerGraveyards.get(player2.getId()).add(drone.getCard());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
