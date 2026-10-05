package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RakdosShredFreak;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Pyroconvergence.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        Terminate.class, RakdosShredFreak.class, JaceArchitectOfThought.class})
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

    @Test
    @DisplayName("A hybrid creature paid for with only red mana triggers before entering")
    void hybridCreatureTriggersBeforeResolving() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        harness.setHand(player1, List.of(new RakdosShredFreak()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Rakdos Shred-Freak");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rakdos Shred-Freak");
    }

    @Test
    @DisplayName("The trigger deals damage to a planeswalker")
    void canDamageAPlaneswalker() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new RakdosShredFreak()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Lethal trigger damage still happens when the triggering spell loses its target")
    void triggerCanKillTheTriggeringSpellsTarget() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RakdosShredFreak());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rakdos Shred-Freak");
        harness.assertInGraveyard(player2, "Rakdos Shred-Freak");

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Terminate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each multicolored spell triggers, including a second spell in the same turn")
    void everyMulticoloredSpellTriggers() {
        harness.addToBattlefield(player1, new Pyroconvergence());
        harness.setHand(player1, List.of(new RakdosShredFreak(), new RakdosShredFreak()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }
}
