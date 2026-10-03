package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColdEyedSelkie.class, Forest.class, GrizzlyBears.class, Island.class})
class ColdEyedSelkieTest extends BaseCardTest {

    @Test
    @DisplayName("May draw cards equal to the combat damage dealt to a player")
    void drawsCardsEqualToCombatDamage() {
        // Two +1/+1 counters make the 1/1 Selkie deal 3 combat damage.
        Permanent selkie = addAttackingSelkie(player1);
        selkie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        // The controller may draw that many cards; accept.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining draws no cards")
    void decliningDrawsNothing() {
        addAttackingSelkie(player1); // 1/1 deals 1
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        addAttackingSelkie(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger a draw")
    void noTriggerWhenCombatDamageIsPrevented() {
        addAttackingSelkie(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        gd.preventAllCombatDamage = true;
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Islandwalk prevents blocking when the defending player controls an Island")
    void islandwalkPreventsBlocking() {
        Permanent selkie = addAttackingSelkie(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(selkie);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("An Island controlled by the attacking player does not prevent blocking")
    void attackingPlayersIslandDoesNotPreventBlocking() {
        Permanent selkie = addAttackingSelkie(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(selkie);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Draw count retains the damage amount when power changes after damage")
    void drawCountUsesDamageAlreadyDealt() {
        Permanent selkie = addAttackingSelkie(player1);
        selkie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        selkie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw ability resolves after the Selkie leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent selkie = addAttackingSelkie(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(selkie);
        gd.playerGraveyards.get(player1.getId()).add(selkie.getCard());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttackingSelkie(Player player) {
        Permanent selkie = addCreatureReady(player, new ColdEyedSelkie());
        selkie.setAttacking(true);
        return selkie;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
