package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormwatchEagleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land returns Stormwatch Eagle to its owner's hand")
    void sacrificeLandReturnsStormwatchEagleToHand() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stormwatch Eagle");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Stormwatch Eagle");
    }

    @Test
    @DisplayName("The controller chooses which land to sacrifice")
    void choosesLandToSacrifice() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Stormwatch Eagle");
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new StormwatchEagle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
