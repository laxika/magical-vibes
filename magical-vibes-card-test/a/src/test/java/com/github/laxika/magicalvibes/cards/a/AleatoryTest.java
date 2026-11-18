package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aleatory.class, FemerefScouts.class, Island.class})
class AleatoryTest extends BaseCardTest {

    private Permanent setUpAndCast() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new FemerefScouts());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        declareAttackers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.currentStep).as("step after blockers are declared").isEqualTo(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }

    @Test
    @DisplayName("On a won flip the target gets +1/+1, on a lost flip it is unchanged")
    void coinFlipDecidesBoost() {
        Permanent target = setUpAndCast();

        boolean won = gameLogContains("wins the coin flip");

        if (won) {
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        } else {
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent target = setUpAndCast();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(0);
    }

    @Test
    @DisplayName("A draw is scheduled regardless of the flip and resolves at the next upkeep")
    void schedulesDrawAtNextUpkeep() {
        setUpAndCast();

        List<TemporaryGlobalTriggeredAbility> scheduled = gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("An illegal target prevents both the coin flip and the delayed draw")
    void illegalTargetPreventsAllEffects() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip")).isFalse();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
        harness.assertInGraveyard(player1, "Aleatory");
    }

    @Test
    @DisplayName("Can target its controller's nonattacking creature at end of combat without drawing immediately")
    void targetsOwnCreatureAtEndOfCombat() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player1, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).hasSize(1);
        int boost = gameLogContains("wins the coin flip") ? 1 : 0;
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1 + boost);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4 + boost);
    }

    @Test
    @DisplayName("Can be cast during the combat damage step")
    void castableDuringCombatDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast before blockers are declared")
    void cannotCastBeforeBlockers() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player2, new FemerefScouts());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Aleatory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        UUID islandId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
