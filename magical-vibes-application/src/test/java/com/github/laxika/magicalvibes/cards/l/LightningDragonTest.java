package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Lightning Dragon at its next upkeep")
    void decliningEchoSacrificesLightningDragon() {
        castAndResolveLightningDragon();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lightning Dragon");
        harness.assertInGraveyard(player1, "Lightning Dragon");
    }

    @Test
    @DisplayName("Paying echo keeps Lightning Dragon and echo does not trigger again")
    void payingEchoKeepsLightningDragonAndIsOneShot() {
        castAndResolveLightningDragon();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Lightning Dragon");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Lightning Dragon");
    }

    @Test
    @DisplayName("Red mana gives Lightning Dragon +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent dragon = addReadyLightningDragon(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    private void castAndResolveLightningDragon() {
        harness.setHand(player1, List.of(new LightningDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lightning Dragon");
    }

    private Permanent addReadyLightningDragon(Player player) {
        Permanent dragon = new Permanent(new LightningDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dragon);
        return dragon;
    }
}
