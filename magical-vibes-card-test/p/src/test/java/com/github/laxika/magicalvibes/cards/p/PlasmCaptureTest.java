package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.s.SavagebornHydra;
import com.github.laxika.magicalvibes.cards.s.Skylasher;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlasmCapture.class, KraulWarrior.class, Skylasher.class, SavagebornHydra.class,
        BonecrusherGiant.class, Stomp.class})
class PlasmCaptureTest extends BaseCardTest {

    /** Player1 casts Kraul Warrior (mana value 2); Player2 holds Plasm Capture to counter it. */
    private KraulWarrior prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PlasmCapture()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        return warrior;
    }

    private void counterWarrior(KraulWarrior warrior) {
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());
    }

    @Test
    @DisplayName("Counters the spell and adds mana in any combination of colors at the caster's next first main phase")
    void countersAndAddsManaAtNextFirstMainPhase() {
        KraulWarrior warrior = prepareCounterTarget();

        counterWarrior(warrior);

        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
        harness.assertInGraveyard(player1, "Kraul Warrior");

        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.controllerId()).isEqualTo(player2.getId());
        assertThat(reward.amount()).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();

        // Mandatory: the delayed ability resolves straight into the color picks, one per mana.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player2, "RED");
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The delayed mana waits for a first main phase and does not fire on a postcombat main")
    void doesNotFireOnPostcombatMain() {
        KraulWarrior warrior = prepareCounterTarget();

        counterWarrior(warrior);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
    }

    private void resolveManaReward(int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        for (int i = 0; i < amount; i++) {
            harness.handleListChoice(player2, "BLUE");
        }
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(amount);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void addsManaEvenWhenTheSpellCannotBeCountered() {
        prepareCounterTarget();
        Skylasher spell = new Skylasher();
        harness.setHand(player1, List.of(spell));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertNotInGraveyard(player1, "Skylasher");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skylasher");
        resolveManaReward(2);
    }

    @Test
    void includesChosenXInSpellManaValue() {
        prepareCounterTarget();
        SavagebornHydra spell = new SavagebornHydra();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Savageborn Hydra");
        resolveManaReward(5);
    }

    @Test
    void usesAdventureManaValueInsteadOfCreatureManaValue() {
        prepareCounterTarget();
        BonecrusherGiant spell = new BonecrusherGiant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Bonecrusher Giant");
        harness.assertLife(player2, 20);
        resolveManaReward(2);
    }

    @Test
    void doesNotAddManaWhenTargetLeavesTheStackBeforeResolution() {
        KraulWarrior spell = prepareCounterTarget();
        harness.setHand(player2, List.of(new PlasmCapture(), new PlasmCapture()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kraul Warrior");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        resolveManaReward(2);
    }

    @Test
    void doesNotTriggerDuringAnotherPlayersFirstMainPhase() {
        KraulWarrior spell = prepareCounterTarget();
        counterWarrior(spell);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        assertThat(gd.stack).isEmpty();
        resolveManaReward(2);
    }
}
