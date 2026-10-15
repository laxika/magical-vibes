package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GutlessGhoul.class, BorealDruid.class})
class GutlessGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gains 2 life")
    void sacrificeAnotherCreatureGainsTwoLife() {
        harness.addToBattlefield(player1, new GutlessGhoul());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Boreal Druid");
        harness.assertOnBattlefield(player1, "Gutless Ghoul");
        assertThat(findPermanent(player1, "Gutless Ghoul").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Ghoul may sacrifice itself")
    void sacrificeSelfGainsTwoLife() {
        harness.addToBattlefield(player1, new GutlessGhoul());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Gutless Ghoul");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ghoul can sacrifice itself and life is gained only on resolution")
    void tappedSummoningSickGhoulCanActivate() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new GutlessGhoul());
        ghoul.tap();
        ghoul.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Gutless Ghoul");
        harness.assertNotOnBattlefield(player1, "Gutless Ghoul");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Without mana the ability cannot sacrifice a creature or gain life")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new GutlessGhoul());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gutless Ghoul");
        harness.assertNotInGraveyard(player1, "Gutless Ghoul");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
