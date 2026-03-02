package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeroOfOxidRidgeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with CantBlockThisTurnEffect(PermanentPowerAtMostPredicate(1))")
    void hasCorrectStructure() {
        HeroOfOxidRidge card = new HeroOfOxidRidge();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(CantBlockThisTurnEffect.class);
        CantBlockThisTurnEffect effect =
                (CantBlockThisTurnEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentPowerAtMostPredicate.class);
    }

    // ===== Attack triggers =====

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());

        declareAttackers(player1, List.of(0));

        // At minimum, the ON_ATTACK trigger should be on the stack (battle cry may also add one)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Hero of Oxid Ridge"));
    }

    // ===== Can't block effect =====

    @Test
    @DisplayName("Creatures with power 1 or less are marked can't block this turn")
    void creaturesWithPower1OrLessCantBlock() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent elves = addCreatureReady(player2, new LlanowarElves()); // 1/1

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(elves.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Creatures with power 2 or more are NOT affected")
    void creaturesWithPower2OrMoreNotAffected() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Effect applies to all players' creatures with power 1 or less")
    void affectsAllPlayersCreatures() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent ownElves = addCreatureReady(player1, new LlanowarElves());   // own 1/1
        Permanent oppElves = addCreatureReady(player2, new LlanowarElves());   // opponent 1/1
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());    // opponent 2/2

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ownElves.isCantBlockThisTurn()).isTrue();
        assertThat(oppElves.isCantBlockThisTurn()).isTrue();
        assertThat(oppBears.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Power-1-or-less creature cannot declare as blocker after trigger resolves")
    void cantBlockPreventsDeclaringBlockers() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent oppElves = addCreatureReady(player2, new LlanowarElves()); // 1/1

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Set up blocker declaration
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to block with the 1/1 should be rejected
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power-2-or-more creature CAN still block after trigger resolves")
    void power2OrMoreCanStillBlock() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Set up blocker declaration
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Grizzly Bears (2/2) should be able to block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(oppBears.isBlocking()).isTrue();
    }

    // Note: Battle cry is engine-handled via the BATTLE_CRY keyword and already
    // tested in AccorderPaladinTest. No need to duplicate those tests here.

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
