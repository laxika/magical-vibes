package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VampiricEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2, flying, and a counter when a creature it damaged dies")
    void enchantedCreatureGetsBoostFlyingAndCounterWhenDamagedCreatureDies() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(enchantedCreature);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not grant a counter when the damaged creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(enchantedCreature);
        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(1);
        blockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, blockerCard);

        enchantedCreature.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player, GrizzlyBears card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new VampiricEmbrace());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
