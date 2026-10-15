package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NefaroxOverlordOfGrixis.class, GrizzlyBears.class, SuntailHawk.class,
        Murder.class, NicolBolasPlaneswalker.class, SearingSpear.class})
class NefaroxOverlordOfGrixisTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes the defending player sacrifice their lone creature")
    void attacksAloneForcesDefenderSacrifice() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("The defending player chooses which creature to sacrifice")
    void defenderChoosesSacrifice() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        // Two triggers go on the stack: exalted and the attack edict.
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, hawk.getId());

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking together with another creature does not force a sacrifice")
    void noSacrificeWhenNotAlone() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void exaltedBoostsLoneAlly() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — Nefarox attacking alone boosts itself")
    void exaltedBoostsSelf() {
        Permanent nefarox = addCreatureReady(player1, new NefaroxOverlordOfGrixis());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nefarox)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, nefarox)).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple attackers receive no exalted bonus")
    void multipleAttackersReceiveNoBonus() {
        Permanent nefarox = addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            harness.passBothPriorities();
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, nefarox)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, nefarox)).isEqualTo(5);
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("The sacrifice trigger survives Nefarox being destroyed in response")
    void sacrificeSurvivesSourceRemoval() {
        Permanent nefarox = addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new NefaroxOverlordOfGrixis());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.castInstant(player2, 0, nefarox.getId());
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Nefarox, Overlord of Grixis");
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        harness.assertInGraveyard(player2, "Nefarox, Overlord of Grixis");
    }

    @Test
    @DisplayName("Attacking a planeswalker makes its controller sacrifice a creature")
    void planeswalkerControllerSacrifices() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new NefaroxOverlordOfGrixis());
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackAtPlaneswalker(bolas);
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        harness.assertInGraveyard(player2, "Nefarox, Overlord of Grixis");
    }

    @Test
    @DisplayName("The defending player still sacrifices after the attacked planeswalker leaves")
    void sacrificeSurvivesAttackedPlaneswalkerRemoval() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new NefaroxOverlordOfGrixis());
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackAtPlaneswalker(bolas);
            harness.castInstant(player1, 0, bolas.getId());
            harness.passBothPriorities();
            harness.assertInGraveyard(player2, "Nicol Bolas, Planeswalker");
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        harness.assertInGraveyard(player2, "Nefarox, Overlord of Grixis");
    }

    private void declareAttackAtPlaneswalker(Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }
}
