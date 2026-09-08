package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryFocus.class, GrizzlyBears.class, RagingGoblin.class})
class PredatoryFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting makes a blocked creature assign all combat damage to the player")
    void acceptingAssignsBlockedDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        castAndChoose(true);
        prepareBlockedAttack(attacker, blocker);
        resolveCombatStep();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Declining leaves blocked combat damage assigned to the blocker")
    void decliningUsesNormalCombatDamageAssignment() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        castAndChoose(false);
        prepareBlockedAttack(attacker, blocker);
        resolveCombatStep();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Accepting also affects creatures entering later that turn")
    void affectsCreaturesEnteringLaterThatTurn() {
        harness.setLife(player2, 20);
        castAndChoose(true);

        Permanent attacker = addReadyCreature(player1, new RagingGoblin());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        prepareBlockedAttack(attacker, blocker);
        resolveCombatStep();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The choice is made while Predatory Focus resolves")
    void asksForChoiceOnResolution() {
        harness.setHand(player1, List.of(new PredatoryFocus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castAndChoose(boolean accept) {
        harness.setHand(player1, List.of(new PredatoryFocus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void prepareBlockedAttack(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void resolveCombatStep() {
        harness.passBothPriorities();
    }
}
