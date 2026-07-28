package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlacialChasmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield sacrifices a land of the controller's choice")
    void enterSacrificesALand() {
        harness.setHand(player1, List.of(new GlacialChasm()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0); // plays the land via playCard
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        harness.assertOnBattlefield(player1, "Glacial Chasm");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Glacial Chasm itself is a legal sacrifice for its own enter trigger")
    void canSacrificeItselfOnEnter() {
        harness.setHand(player1, List.of(new GlacialChasm()));
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent chasm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Glacial Chasm".equals(p.getCard().getName()))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(chasm.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chasm);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Glacial Chasm");
    }

    @Test
    @DisplayName("Noncombat damage to the controller is prevented entirely")
    void preventsNoncombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GlacialChasm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Combat damage to the controller is prevented")
    void preventsCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GlacialChasm());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage to the controller's creatures is not prevented")
    void doesNotPreventDamageToOwnCreatures() {
        harness.addToBattlefield(player1, new GlacialChasm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures the controller controls can't attack")
    void controlledCreaturesCannotAttack() {
        harness.addToBattlefield(player1, new GlacialChasm());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack restriction is controller-scoped — opponents may still attack")
    void opponentCreaturesCanStillAttack() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GlacialChasm());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        // The declaration is legal (player2's Chasm restricts only player2's creatures); the
        // damage it would deal is then prevented because player2 controls the Chasm.
        gs.declareAttackers(gd, player1, List.of(bearsIndex));

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep costs 2 life per age counter")
    void paysCumulativeUpkeepInLife() {
        Permanent chasm = harness.addToBattlefieldAndReturn(player1, new GlacialChasm());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(chasm.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chasm);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Second upkeep costs 4 life")
    void secondUpkeepCostsFourLife() {
        Permanent chasm = harness.addToBattlefieldAndReturn(player1, new GlacialChasm());
        chasm.setCounterCount(CounterType.AGE, 1);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(chasm.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chasm);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Glacial Chasm")
    void decliningUpkeepSacrifices() {
        Permanent chasm = harness.addToBattlefieldAndReturn(player1, new GlacialChasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chasm);
        harness.assertInGraveyard(player1, "Glacial Chasm");
    }
}
