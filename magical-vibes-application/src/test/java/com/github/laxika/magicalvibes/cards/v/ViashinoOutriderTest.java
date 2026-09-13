package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
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

@CardUsed({ViashinoOutrider.class, ControlMagic.class})
class ViashinoOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Viashino Outrider at its next upkeep")
    void decliningEchoSacrificesViashinoOutrider() {
        castAndResolveViashinoOutrider();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Viashino Outrider");
        harness.assertInGraveyard(player1, "Viashino Outrider");
    }

    @Test
    @DisplayName("Echo does not trigger during the opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveViashinoOutrider();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Viashino Outrider");
    }

    @Test
    @DisplayName("Gaining control of Viashino Outrider creates a new echo trigger")
    void gainingControlCreatesNewEchoTrigger() {
        castAndResolveViashinoOutrider();
        Permanent outrider = findPermanent(player1, "Viashino Outrider");

        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player2, 0, outrider.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Viashino Outrider");
        harness.assertOnBattlefield(player2, "Viashino Outrider");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0, outrider.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Viashino Outrider");
        harness.assertOnBattlefield(player1, "Viashino Outrider");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Viashino Outrider");
        harness.assertInGraveyard(player1, "Viashino Outrider");
    }

    @Test
    @DisplayName("Paying echo keeps Viashino Outrider and echo does not trigger again")
    void payingEchoKeepsViashinoOutriderAndIsOneShot() {
        castAndResolveViashinoOutrider();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Viashino Outrider");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Viashino Outrider");
    }

    private void castAndResolveViashinoOutrider() {
        harness.castFromHand(player1, new ViashinoOutrider(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Viashino Outrider");
    }
}
