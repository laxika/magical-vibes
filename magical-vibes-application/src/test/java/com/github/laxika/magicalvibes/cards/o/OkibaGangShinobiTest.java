package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChildOfThorns;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OkibaGangShinobi.class, ChildOfThorns.class})
class OkibaGangShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard two cards of their choice")
    void combatDamageMakesDamagedPlayerDiscardTwo() {
        Permanent shinobi = addCreatureReady(player1, new OkibaGangShinobi());
        shinobi.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(
                new ChildOfThorns(), new ChildOfThorns(), new ChildOfThorns())));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches the player")
    void noTriggerWhenBlocked() {
        Permanent shinobi = addCreatureReady(player1, new OkibaGangShinobi());
        shinobi.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ChildOfThorns());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new ChildOfThorns(), new ChildOfThorns())));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts the Shinobi in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new ChildOfThorns());
        addCreatureReady(player2, new ChildOfThorns());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OkibaGangShinobi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Child of Thorns");
        Permanent shinobi = findPermanent(player1, "Okiba-Gang Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new ChildOfThorns());
        addCreatureReady(player2, new ChildOfThorns());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OkibaGangShinobi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }
}
