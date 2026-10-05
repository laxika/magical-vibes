package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OdylicWraith.class, GrizzlyBears.class, Forest.class, Swamp.class, FireWhip.class})
class OdylicWraithTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card of their choice")
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingWraith(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No discard when the Wraith is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingWraith(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Empty hand means nothing is discarded and no prompt appears")
    void noPromptWithEmptyHand() {
        addAttackingWraith(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking when defending player controls a Swamp")
    void swampwalkPreventsBlockingWithSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new OdylicWraith());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk allows blocking when defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutSwamp() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new OdylicWraith());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent makes that opponent discard one card")
    void noncombatDamageMakesOpponentDiscard() {
        Permanent wraith = addCreatureReady(player1, new OdylicWraith());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(wraith.getId());
        harness.setHand(player2, List.of(new Forest(), new Swamp()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Swamp");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Damage to the Wraith's controller makes its controller discard")
    void noncombatDamageToControllerMakesControllerDiscard() {
        Permanent wraith = addCreatureReady(player1, new OdylicWraith());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(wraith.getId());
        harness.setHand(player1, List.of(new Forest(), new Swamp()));
        harness.setHand(player2, List.of(new Forest(), new Swamp()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Damage dealt by Fire Whip itself does not trigger the enchanted Wraith")
    void auraDamageDoesNotTriggerWraith() {
        Permanent wraith = addCreatureReady(player1, new OdylicWraith());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(wraith.getId());
        harness.setHand(player2, List.of(new Forest(), new Swamp()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Fire Whip");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingWraith(Player player) {
        Permanent wraith = addCreatureReady(player, new OdylicWraith());
        wraith.setAttacking(true);
        return wraith;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
