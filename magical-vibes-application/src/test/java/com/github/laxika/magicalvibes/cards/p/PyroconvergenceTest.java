package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyroconvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell deals 2 damage to the chosen target")
    void multicoloredSpellDealsDamage() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, victim.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // Pyroconvergence trigger
        harness.passBothPriorities(); // Terminate

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a monocolored spell does not trigger")
    void monocoloredSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Pyroconvergence());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertLife(player2, 17); // only the Bolt
    }

    @Test
    @DisplayName("An opponent casting a multicolored spell does not trigger")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, victim.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The trigger can be pointed at a creature")
    void canDamageACreature() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.handlePermanentChosen(player1, bystander.getId());

        harness.passBothPriorities(); // Pyroconvergence trigger
        harness.passBothPriorities(); // Terminate

        assertThat(bystander.getMarkedDamage()).isEqualTo(2);
    }
}
