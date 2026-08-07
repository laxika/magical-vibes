package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShellOfTheLastKappaTest extends BaseCardTest {

    /**
     * player2 bolts player1, who responds by exiling the Bolt with the Shell. Returns the Shell's
     * permanent so callers can untap it for the sacrifice ability.
     */
    private Permanent boltPlayerOneAndExileIt(LightningBolt bolt) {
        Permanent shell = harness.addToBattlefieldAndReturn(player1, new ShellOfTheLastKappa());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, new ArrayList<>(List.of(bolt)));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, 0, null, bolt.getId());
        harness.passBothPriorities();
        return shell;
    }

    @Test
    @DisplayName("First ability exiles an instant spell that targets you, so it never deals damage")
    void exilesInstantTargetingYou() {
        LightningBolt bolt = new LightningBolt();
        Permanent shell = boltPlayerOneAndExileIt(bolt);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.exiledCards)
                .anyMatch(e -> shell.getId().equals(e.sourcePermanentId())
                        && e.card().getId().equals(bolt.getId()));
    }

    @Test
    @DisplayName("A spell that targets a creature you control is not a legal target")
    void cannotExileSpellTargetingYourCreature() {
        harness.addToBattlefield(player1, new ShellOfTheLastKappa());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, new ArrayList<>(List.of(bolt)));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bolt.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Sacrifice ability lets the Shell's controller cast an exiled spell for free")
    void sacrificeAbilityCastsExiledSpellForFree() {
        LightningBolt bolt = new LightningBolt();
        Permanent shell = boltPlayerOneAndExileIt(bolt);
        harness.passBothPriorities();

        shell.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // The Bolt is now player1's spell, so it can be aimed back at player2.
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Shell of the Last Kappa");
        assertThat(gd.exiledCards).noneMatch(e -> shell.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Declining the free cast leaves the exiled card in exile")
    void decliningLeavesCardExiled() {
        LightningBolt bolt = new LightningBolt();
        Permanent shell = boltPlayerOneAndExileIt(bolt);
        harness.passBothPriorities();

        shell.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getId().equals(bolt.getId()));
    }
}
