package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicRenewalTest extends BaseCardTest {

    /** Player 2 edicts away player 1's only creature, firing Angelic Renewal's death trigger. */
    private void killPlayerOnesCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        // Pass 1: Cruel Edict resolves and the creature dies. Pass 2: the death trigger resolves and
        // Angelic Renewal's controller is asked whether to sacrifice it.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting sacrifices the enchantment and returns the dead creature to the battlefield")
    void acceptReturnsCreature() {
        harness.addToBattlefield(player1, new AngelicRenewal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killPlayerOnesCreature();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Angelic Renewal");
        harness.assertInGraveyard(player1, "Angelic Renewal");
    }

    @Test
    @DisplayName("Declining leaves the enchantment on the battlefield and the creature in the graveyard")
    void declineKeepsEnchantment() {
        harness.addToBattlefield(player1, new AngelicRenewal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killPlayerOnesCreature();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Angelic Renewal");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger on an opponent's creature dying")
    void doesNotTriggerOnOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new AngelicRenewal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Angelic Renewal");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
