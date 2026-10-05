package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.j.JaceUnravelerOfSecrets;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvocationOfSaintTraft.class, DevilthornFox.class, JaceUnravelerOfSecrets.class})
class InvocationOfSaintTraftTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature creates a tapped and attacking Angel")
    void attackCreatesAngelToken() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);
        attackAndResolve(player1, List.of(0));

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttacking()).isTrue();
        assertThat(angel.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The Angel token is exiled at end of combat")
    void angelTokenExiledAtEndOfCombat() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);
        attackAndResolve(player1, List.of(0));

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        angel.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
    }

    @Test
    @DisplayName("The Angel token is created for the enchanted creature's controller")
    void tokenGoesToEnchantedController() {
        Permanent creature = addCreatureReady(player2, new DevilthornFox());
        attachInvocation(player1, creature);
        attackAndResolve(player2, List.of(0));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("The creature's controller controls the granted attack trigger")
    void enchantedCreatureControllerControlsTrigger() {
        Permanent creature = addCreatureReady(player2, new DevilthornFox());
        attachInvocation(player1, creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("The enchanted creature is the source of the granted attack trigger")
    void enchantedCreatureIsTriggerSource() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(creature.getId());
        });
    }

    @Test
    @DisplayName("The Angel's attack destination is chosen when the defender has a planeswalker")
    void angelAttackDestinationCanDifferFromCreature() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);
        harness.addToBattlefield(player2, new JaceUnravelerOfSecrets());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
        });
    }

    @Test
    @DisplayName("Each attached Invocation creates its own Angel")
    void multipleInvocationsCreateMultipleAngels() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);
        attachInvocation(player1, creature);

        attackAndResolve(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("An unenchanted attacker does not create an Angel")
    void otherAttackerDoesNotTriggerInvocation() {
        Permanent enchanted = addCreatureReady(player1, new DevilthornFox());
        addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, enchanted);

        attackAndResolve(player1, List.of(1));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("End-of-combat exile waits for the delayed trigger to resolve")
    void exileUsesTheStack() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);
        attackAndResolve(player1, List.of(0));
        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.forceStep(TurnStep.COMBAT_DAMAGE);
            harness.clearPriorityPassed();
            harness.passBothPriorities();
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
        });
    }

    @Test
    @DisplayName("Removing the Aura after the attack trigger does not stop token creation or exile")
    void auraRemovalDoesNotStopTriggeredAbility() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        Permanent aura = attachInvocation(player1, creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .destroyPermanentToGraveyard(gd, aura));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken())).hasSize(1);
        });
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Removing the enchanted creature after triggering does not stop the Angel")
    void creatureRemovalDoesNotStopTriggeredAbility() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        attachInvocation(player1, creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .destroyPermanentToGraveyard(gd, creature));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken())).hasSize(1);
        });
    }

    private void attackAndResolve(Player player, List<Integer> attackerIndices) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player, attackerIndices);
            resolveAllTriggers();
        });
    }

    private Permanent attachInvocation(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new InvocationOfSaintTraft());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
