package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartialLaw.class, GrizzlyBears.class, LlanowarElves.class})
class MartialLawTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents target selection")
    void upkeepTriggerPresentsTargetSelection() {
        harness.addToBattlefield(player1, new MartialLaw());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        Permanent bears = detainOpponentCreature(new GrizzlyBears());

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        detainOpponentCreature(new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        detainOpponentCreature(new LlanowarElves());

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the Martial Law controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bears = detainOpponentCreature(new GrizzlyBears());

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not trigger when the opponent controls no creatures")
    void doesNotTriggerWithoutOpponentCreatures() {
        harness.addToBattlefield(player1, new MartialLaw());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MartialLaw());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Detain survives the beginning of the opponent's turn")
    void detainSurvivesOpponentTurnStart() {
        Permanent bears = detainOpponentCreature(new GrizzlyBears());

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detain persists after Martial Law leaves the battlefield")
    void detainPersistsAfterSourceLeaves() {
        Permanent bears = detainOpponentCreature(new GrizzlyBears());
        Permanent martialLaw = findPermanent(player1, "Martial Law");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, martialLaw));

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An upkeep trigger resolves after Martial Law leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new MartialLaw());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent martialLaw = findPermanent(player1, "Martial Law");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, martialLaw));
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Only opposing creatures are offered as upkeep targets")
    void onlyOpposingCreaturesAreOfferedAsTargets() {
        harness.addToBattlefield(player1, new MartialLaw());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MartialLaw());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opposingCreature.getId());
    }

    @Test
    @DisplayName("A creature entering after the chosen target leaves is not detained")
    void doesNotDetainReplacementForDepartedTarget() {
        harness.addToBattlefield(player1, new MartialLaw());
        Permanent original = addCreatureReady(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, original.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));
        Permanent replacement = addCreatureReady(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThatCode(() -> declareAttack(replacement)).doesNotThrowAnyException();
    }

    /** Resolves the upkeep trigger detaining the given creature put onto player2's battlefield. */
    private Permanent detainOpponentCreature(com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(player1, new MartialLaw());
        Permanent target = addCreatureReady(player2, card);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        return target;
    }

    /** Attempts to declare the given player2 creature as an attacker. */
    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
