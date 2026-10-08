package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnakeCultInitiation.class, GrizzlyBears.class, FountainOfYouth.class})
class SnakeCultInitiationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has poisonous and gives three poison counters on combat damage")
    void enchantedCreatureHasPoisonousAndPoisonsOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        attachAura(creature);
        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.POISONOUS)).isTrue();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Poisonous ability does not trigger when the enchanted creature is blocked")
    void doesNotPoisonWhenBlocked() {
        Permanent creature = addReadyCreature(player1);
        attachAura(creature);
        Permanent blocker = addReadyCreature(player2);
        creature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        harness.setLife(player1, 20);
        Permanent creature = addReadyCreature(player2);
        attachAura(creature);
        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.POISONOUS)).isTrue();

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Poisonous ability ends when Snake Cult Initiation leaves the battlefield")
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.POISONOUS)).isFalse();
    }

    @Test
    @DisplayName("Snake Cult Initiation can enchant only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SnakeCultInitiation()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting Snake Cult Initiation attaches it and grants poisonous 3")
    void castingAuraGrantsPoisonous() {
        Permanent creature = addReadyCreature(player1);
        harness.setHand(player1, List.of(new SnakeCultInitiation()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Snake Cult Initiation").getAttachedTo()).isEqualTo(creature.getId());
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Snake Cult Initiations grant two separately resolving poisonous abilities")
    void multipleAurasTriggerSeparately() {
        Permanent creature = addReadyCreature(player1);
        attachAura(creature);
        attachAura(creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(6);
    }

    @Test
    @DisplayName("Poisonous resolves after the Aura and enchanted creature leave the battlefield")
    void poisonousTriggerSurvivesSourceLeaving() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachAura(creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removing one of two Auras before damage leaves one poisonous ability")
    void onlyRemainingAuraGrantsPoisonous() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachAura(creature);
        attachAura(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Poisonous 3 causes a player with seven poison counters to lose on resolution")
    void poisonousReachesLethalThreshold() {
        gd.playerPoisonCounters.put(player2.getId(), 7);
        Permanent creature = addReadyCreature(player1);
        attachAura(creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(7);

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeCultInitiation());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
