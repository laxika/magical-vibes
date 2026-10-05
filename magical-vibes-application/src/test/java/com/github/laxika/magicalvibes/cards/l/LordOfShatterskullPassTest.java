package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.PelakkaWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfShatterskullPass.class, PelakkaWurm.class, InvasionOfZendikar.class, AwakenedSkyclave.class, GideonJura.class})
class LordOfShatterskullPassTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Lord of Shatterskull Pass's power and toughness")
    void levelsUpAtThresholds() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(lord, 6, 6);

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(lord, 6, 6);

        levelUp(player1);

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        assertStats(lord, 6, 6);
    }

    @Test
    @DisplayName("At level 6, attacking Lord of Shatterskull Pass deals 6 damage to each defending creature")
    void levelSixAttackTriggerDamagesDefendingCreatures() {
        addCreatureReady(player1, new LordOfShatterskullPass());
        Permanent defendingCreature = addCreatureReady(player2, new PelakkaWurm());
        Permanent ownCreature = addCreatureReady(player1, new PelakkaWurm());

        prepareForLeveling(player1);
        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingCreature.getMarkedDamage()).isEqualTo(6);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Lord of Shatterskull Pass's attack trigger is inactive below level 6")
    void attackTriggerIsInactiveBelowLevelSix() {
        addCreatureReady(player1, new LordOfShatterskullPass());
        Permanent defendingCreature = addCreatureReady(player2, new PelakkaWurm());

        prepareForLeveling(player1);
        for (int i = 0; i < 5; i++) {
            levelUp(player1);
        }

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Level up uses the stack and does not require tapping")
    void levelUpResolvesBeforeCounterIsAdded() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        lord.setTapped(true);
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isZero();
        harness.passBothPriorities();
        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(lord, 6, 6);
        assertThat(lord.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Level up cannot be activated outside a main phase")
    void levelUpRequiresMainPhase() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(lord.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("Level up cannot be activated in response to another level up")
    void levelUpRequiresEmptyStack() {
        addCreatureReady(player1, new LordOfShatterskullPass());
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Level up requires red mana even when enough generic mana is available")
    void levelUpRequiresRedMana() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(lord.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("An attack trigger still deals damage after level counters are removed")
    void attackTriggerSurvivesLossOfLevelCounters() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        lord.setCounterCount(CounterType.LEVEL, 6);
        Permanent defender = addCreatureReady(player2, new PelakkaWurm());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        lord.setCounterCount(CounterType.LEVEL, 0);
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Above level 6 the trigger damages every defending creature and kills small ones")
    void higherLevelsDamageAllDefendingCreatures() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        lord.setCounterCount(CounterType.LEVEL, 7);
        Permanent largeDefender = addCreatureReady(player2, new PelakkaWurm());
        Permanent smallDefender = addCreatureReady(player2, new LordOfShatterskullPass());
        Permanent ally = addCreatureReady(player1, new PelakkaWurm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(largeDefender.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(smallDefender);
        harness.assertInGraveyard(player2, "Lord of Shatterskull Pass");
        assertThat(ally.getMarkedDamage()).isZero();
        assertThat(lord.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Attacking a battle damages its protector's creatures, not its controller's")
    void attackingBattleDamagesProtectorsCreatures() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        lord.setCounterCount(CounterType.LEVEL, 6);
        Permanent ally = addCreatureReady(player1, new PelakkaWurm());
        Permanent defender = addCreatureReady(player2, new PelakkaWurm());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(6);
        assertThat(ally.getMarkedDamage()).isZero();
        assertThat(lord.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({GideonJura.class})
    @DisplayName("The attack trigger still damages defending creatures after the attacked planeswalker leaves")
    void attackTriggerSurvivesPlaneswalkerDeparture() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        lord.setCounterCount(CounterType.LEVEL, 6);
        Permanent defender = addCreatureReady(player2, new PelakkaWurm());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(6);
    }

    private void prepareForLeveling(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.RED, 12);
    }

    private void levelUp(com.github.laxika.magicalvibes.model.Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
