package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperState.class, GrizzlyBears.class})
class SuperStateTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes 9/9 and gains the listed keywords")
    void enchantedCreatureGetsBaseStatsAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuperState()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Aura can enchant only a creature its controller controls")
    void onlyTargetsCreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuperState()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The combat trigger does not deal extra damage in a two-player game")
    void combatTriggerExcludesTheDamagedOpponent() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuperState(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    void countersApplyOnTopOfBasePowerAndToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attachSuperState(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
    }

    @Test
    void auraBecomesIllegalWhenCreatureChangesController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuperState(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Super State");
        harness.assertNotOnBattlefield(player1, "Super State");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void combatDamageIsRepeatedToOtherOpponents() {
        Player player3 = addThirdPlayer();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuperState(player1, creature);
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        harness.assertLife(player3, 11);
    }

    @Test
    void combatTriggerStillDealsDamageAfterEnchantedCreatureLeaves() {
        Player player3 = addThirdPlayer();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuperState(player1, creature);
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.runStateBasedActions();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        harness.assertLife(player3, 11);
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

    private Permanent attachSuperState(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new SuperState());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
