package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BearerOfSilence.class, GrizzlyBears.class})
class BearerOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, paying {1}{C} makes the targeted opponent sacrifice a creature")
    void payingCastTriggerCostSacrificesTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);


        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bearer of Silence");
    }

    @Test
    @DisplayName("Declining the cast trigger payment does not sacrifice a creature")
    void decliningCastTriggerPaymentDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bearer of Silence");
    }

    @Test
    @DisplayName("The cast trigger cannot target its controller")
    void castTriggerCannotTargetController() {
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bearer of Silence cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player2, new BearerOfSilence());
        bearer.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Colored mana cannot pay the colorless part of the cast trigger cost")
    void coloredManaCannotPayColorlessCost() {
        harness.addToBattlefield(player2, new BearerOfSilence());
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Bearer of Silence");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bearer of Silence");
    }

    @Test
    @DisplayName("The payment can be made even when the target opponent has no creatures")
    void canPayWithNoOpposingCreatures() {
        harness.addToBattlefield(player1, new BearerOfSilence());
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Entering the battlefield without being cast does not trigger the sacrifice ability")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new BearerOfSilence());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.enterBattlefieldAndReturn(player1, new BearerOfSilence());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Bearer of Silence");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The targeted opponent chooses which of their creatures to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new BearerOfSilence());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new BearerOfSilence());
        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player2, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(kept);
        harness.assertInGraveyard(player2, "Bearer of Silence");
        harness.assertNotOnBattlefield(player1, "Bearer of Silence");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bearer of Silence");
    }
}
