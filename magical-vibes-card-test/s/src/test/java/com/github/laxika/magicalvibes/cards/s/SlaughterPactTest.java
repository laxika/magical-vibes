package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterPact.class, BlindPhantasm.class, MassOfGhouls.class, ZoeticCavern.class})
class SlaughterPactTest extends BaseCardTest {

    private void castPact() {
        Permanent creature = addCreatureReady(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(new SlaughterPact()));

        harness.castAndResolveInstant(player1, 0, creature.getId());
    }

    private void reachPactUpkeepPrompt() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys a target nonblack creature and schedules its upkeep payment")
    void destroysCreatureAndSchedulesPayment() {
        castPact();

        harness.assertNotOnBattlefield(player2, "Blind Phantasm");
        harness.assertInGraveyard(player2, "Blind Phantasm");

        List<PayManaOrLoseGameAtNextUpkeep> scheduled = gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().manaCost()).isEqualTo("{2}{B}");
    }

    @Test
    @DisplayName("Waits for the controller's next upkeep")
    void waitsForControllerNextUpkeep() {
        castPact();
        advanceToUpkeep(player2);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Paying {2}{B} at the next upkeep avoids losing the game")
    void payingAvoidsLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Declining the next-upkeep payment loses the game")
    void decliningCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Being unable to pay at the next upkeep loses the game")
    void beingUnableToPayCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent creature = addCreatureReady(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new SlaughterPact()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ZoeticCavern());
        harness.setHand(player1, List.of(new SlaughterPact()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("A Pact whose target is destroyed in response creates no payment obligation")
    void illegalTargetDoesNotSchedulePayment() {
        Permanent creature = addCreatureReady(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(new SlaughterPact()));
        harness.setHand(player2, List.of(new SlaughterPact()));

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slaughter Pact");
        harness.assertInGraveyard(player2, "Blind Phantasm");
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class))
                .singleElement().satisfies(action -> assertThat(action.playerId()).isEqualTo(player2.getId()));
        advanceToUpkeep(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Can destroy a nonblack creature controlled by the caster")
    void canDestroyOwnCreature() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new SlaughterPact()));

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Blind Phantasm");
        harness.assertInGraveyard(player1, "Blind Phantasm");
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Three mana without black mana cannot pay the upkeep cost")
    void paymentRequiresBlackMana() {
        castPact();
        reachPactUpkeepPrompt();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A paid Pact does not require another payment on a later upkeep")
    void paymentOccursOnlyOnce() {
        castPact();
        reachPactUpkeepPrompt();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Can destroy a colorless face-down Zoetic Cavern")
    void canDestroyColorlessCreature() {
        harness.setHand(player1, List.of(new ZoeticCavern(), new SlaughterPact()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent cavern = findPermanent(player1, "Zoetic Cavern");

        harness.castAndResolveInstant(player1, 0, cavern.getId());

        harness.assertNotOnBattlefield(player1, "Zoetic Cavern");
        harness.assertInGraveyard(player1, "Zoetic Cavern");
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("A target that turns face up into a land prevents the entire Pact from resolving")
    void targetBecomingNoncreatureDoesNotSchedulePayment() {
        harness.setHand(player1, List.of(new ZoeticCavern(), new SlaughterPact()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent cavern = findPermanent(player1, "Zoetic Cavern");
        harness.castInstant(player1, 0, cavern.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cavern));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zoetic Cavern");
        harness.assertInGraveyard(player1, "Slaughter Pact");
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }
}
