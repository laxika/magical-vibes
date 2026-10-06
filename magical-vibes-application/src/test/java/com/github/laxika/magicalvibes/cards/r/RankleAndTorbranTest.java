package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RankleAndTorbran.class, GrizzlyBears.class, InvasionOfInnistrad.class,
        SerraAngel.class, Shock.class})
class RankleAndTorbranTest extends BaseCardTest {

    private static final String TREASURE = "Each player creates a Treasure token.";
    private static final String SACRIFICE = "Each player sacrifices a creature of their choice.";
    private static final String DAMAGE =
            "If a source would deal damage to a player or battle this turn, it deals that much damage plus 2 instead.";

    @Test
    @DisplayName("Combat damage trigger can make each player create a Treasure")
    void createsTreasureForEachPlayer() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(TREASURE);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage trigger gathers each player's creature choice before sacrificing")
    void sacrificesOneCreatureFromEachPlayer() {
        addAttackingRankle();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(SACRIFICE);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentCreature.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Rankle and Torbran"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Damage mode adds two to player and battle damage but not creature damage")
    void addsDamageOnlyToPlayersAndBattles() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(DAMAGE);

        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, battle.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Modes must be chosen before players can respond to the combat damage trigger")
    void choosesModesBeforeTriggerResolution() {
        addAttackingRankle();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Choosing no modes produces no Treasure or sacrifice")
    void canChooseNoModes() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Rankle and Torbran")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("First strike damage mode increases a later attacker's regular combat damage")
    void increasesRegularCombatDamageAfterFirstStrike() {
        addAttackingRankle();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(DAMAGE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Combat damage to a battle triggers the Treasure mode")
    void triggersWhenDamagingBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        Permanent rankle = addCreatureReady(player1, new RankleAndTorbran());
        rankle.setAttacking(true);
        rankle.setAttackTarget(battle.getId());
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(TREASURE);

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("All modes resolve in printed order even when Rankle and Torbran is sacrificed")
    void allModesContinueAfterSourceIsSacrificed() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, DAMAGE);
        harness.handleListChoice(player1, SACRIFICE);
        harness.handleListChoice(player1, TREASURE);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Rankle and Torbran")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rankle and Torbran"));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage mode increases regular combat damage to a battle")
    void increasesRegularCombatDamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        addAttackingRankle();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);
        bear.setAttackTarget(battle.getId());
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(DAMAGE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage bonus expires before the next turn")
    void damageBonusExpiresAtEndOfTurn() {
        addAttackingRankle();
        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        chooseMode(DAMAGE);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void addAttackingRankle() {
        Permanent rankle = addCreatureReady(player1, new RankleAndTorbran());
        rankle.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, "Done");
    }
}
