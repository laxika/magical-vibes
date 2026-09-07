package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SnakeCultInitiation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new SnakeCultInitiation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
