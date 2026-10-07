package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalkingLeonin.class})
class StalkingLeoninTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the chosen player exiles their creature attacking you")
    void exilesChosenPlayersAttackingCreature() {
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent attacker = addAttacker(player2, player1, new StalkingLeonin());

        prepareActivation();
        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(attacker.getCard().getId());
    }

    @Test
    @DisplayName("The ability rejects a creature that is not attacking you")
    void rejectsNonAttackingCreature() {
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent creature = addCreatureReady(player2, new StalkingLeonin());

        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can be activated only once")
    void activatesOnlyOnce() {
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent attacker = addAttacker(player2, player1, new StalkingLeonin());
        Permanent secondAttacker = addAttacker(player2, player1, new StalkingLeonin());

        prepareActivation();
        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());
        harness.passBothPriorities();

        prepareActivation();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, secondAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Choosing an opponent is a triggered ability that uses the stack")
    void opponentChoiceUsesStack() {
        castStalkingLeonin();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        chooseOpponent();
    }

    @Test
    @DisplayName("A creature attacking another player is not attacking you")
    void rejectsCreatureAttackingAnotherPlayer() {
        Player thirdPlayer = addOpponent();
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent attacker = addAttacker(player2, thirdPlayer, new StalkingLeonin());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacker that stops attacking before resolution is not exiled")
    void doesNotExileCreatureThatStopsAttacking() {
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent attacker = addAttacker(player2, player1, new StalkingLeonin());
        prepareActivation();
        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .doesNotContain(attacker.getCard().getId());
        Permanent secondAttacker = addAttacker(player2, player1, new StalkingLeonin());
        prepareActivation();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(leonin), null, secondAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("An attacker controlled by an unchosen opponent is still a legal target")
    void canTargetUnchosenOpponentsAttacker() {
        Player thirdPlayer = addOpponent();
        Permanent leonin = castStalkingLeonin();
        chooseOpponent();
        Permanent attacker = addAttacker(thirdPlayer, player1, new StalkingLeonin());
        prepareActivation();

        harness.activateAbility(player1, battlefieldIndex(leonin), null, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(thirdPlayer.getId())).contains(attacker);
    }

    private Player addOpponent() {
        UUID playerId = UUID.randomUUID();
        Player opponent = new Player(playerId, "Charlie");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(playerId, "Charlie");
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), playerId, "Charlie");
        return opponent;
    }

    private Permanent castStalkingLeonin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new StalkingLeonin(), "{2}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Stalking Leonin");
    }

    private void chooseOpponent() {
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = addCreatureReady(controller, card);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.ensurePriority(player1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
