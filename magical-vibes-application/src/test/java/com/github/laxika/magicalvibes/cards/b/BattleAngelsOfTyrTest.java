package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleAngelsOfTyr.class, Forest.class})
class BattleAngelsOfTyrTest extends BaseCardTest {

    @Test
    void combatDamageAppliesEachRiderWhenDamagedPlayerLeads() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);

        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());
        angels.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }

    @Test
    void combatDamageSkipsRidersWhenDamagedPlayerDoesNotLead() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());
        angels.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void landAndLifeRewardsDoNotRequireTheHandReward() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());
        angels.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void rewardsCompareStateAtResolutionRatherThanWhenDamageWasDealt() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());
        angels.setAttacking(true);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 30);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void tiedThirdPlayerPreventsEachReward() {
        Player thirdPlayer = addThirdPlayer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(thirdPlayer, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(thirdPlayer, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        harness.setLife(thirdPlayer, 26);
        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());
        angels.setAttacking(true);
        angels.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void myriadCreatesAttackingCopyAndExilesItAtEndOfCombat() {
        Player thirdPlayer = addThirdPlayer();
        Permanent angels = addCreatureReady(player1, new BattleAngelsOfTyr());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Battle Angels of Tyr").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(thirdPlayer.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy).contains(angels);
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }
}
