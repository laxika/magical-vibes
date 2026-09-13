package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormwatchEagle.class, WintermoonMesa.class})
class StormwatchEagleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land returns Stormwatch Eagle to its owner's hand")
    void sacrificeLandReturnsStormwatchEagleToHand() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stormwatch Eagle");
        harness.assertInGraveyard(player1, "Wintermoon Mesa");
        harness.assertNotOnBattlefield(player1, "Stormwatch Eagle");
    }

    @Test
    @DisplayName("The controller chooses which land to sacrifice")
    void choosesLandToSacrifice() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
        harness.assertInGraveyard(player1, "Wintermoon Mesa");
        harness.assertInHand(player1, "Stormwatch Eagle");
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new StormwatchEagle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The returned creature goes to its owner's hand")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        StormwatchEagle eagle = new StormwatchEagle();
        eagle.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, eagle);
        harness.addToBattlefield(player2, new WintermoonMesa());

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stormwatch Eagle");
        harness.assertNotInHand(player2, "Stormwatch Eagle");
        harness.assertNotOnBattlefield(player2, "Stormwatch Eagle");
    }

    @Test
    @DisplayName("The ability cannot sacrifice an opponent's land")
    void requiresControllerToControlLand() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefield(player2, new WintermoonMesa());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
