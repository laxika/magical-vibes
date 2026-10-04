package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlyingDrone.class, SkyshroudFalcon.class, GrizzlyBears.class, Levitation.class})
class FlyingDroneTest extends BaseCardTest {

    @Test
    @DisplayName("The Drone's ability normally costs {1}{U}")
    void abilityNormallyCostsOneBlueAndOneGeneric() {
        addCreatureReady(player1, new FlyingDrone());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The Drone itself does not satisfy its own discount")
    void sourceIsNotAnotherCreature() {
        Permanent drone = harness.enterBattlefieldAndReturn(player1, new FlyingDrone());
        drone.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A flying creature entering this turn makes the ability free")
    void flyingCreatureEntryReducesFullCostAndResolvesLoot() {
        Permanent drone = addCreatureReady(player1, new FlyingDrone());
        harness.enterBattlefieldAndReturn(player1, new SkyshroudFalcon());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(drone.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("A non-flying creature does not make the ability free")
    void nonFlyingCreatureEntryDoesNotReduceCost() {
        addCreatureReady(player1, new FlyingDrone());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void opponentsFlyingCreatureDoesNotReduceCost() {
        addCreatureReady(player1, new FlyingDrone());
        harness.enterBattlefieldAndReturn(player2, new SkyshroudFalcon());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void paidActivationDrawsBeforeDiscardingAndCanKeepDrawnCard() {
        Permanent drone = addCreatureReady(player1, new FlyingDrone());
        Card original = new GrizzlyBears();
        Card drawn = new SkyshroudFalcon();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original);
    }

    @Test
    void freeActivationStillRequiresAnUntappedSource() {
        Permanent drone = addCreatureReady(player1, new FlyingDrone());
        drone.tap();
        harness.enterBattlefieldAndReturn(player1, new SkyshroudFalcon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeActivationStillRequiresSourceNotToBeSummoningSick() {
        harness.enterBattlefieldAndReturn(player1, new FlyingDrone());
        harness.enterBattlefieldAndReturn(player1, new SkyshroudFalcon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flyingGrantedAsCreatureEntersQualifiesForDiscount() {
        addCreatureReady(player1, new FlyingDrone());
        harness.addToBattlefield(player1, new Levitation());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new SkyshroudFalcon();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }
}
