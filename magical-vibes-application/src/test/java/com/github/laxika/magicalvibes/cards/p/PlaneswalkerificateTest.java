package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Planeswalkerificate.class, GrizzlyBears.class})
class PlaneswalkerificateTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a creature into a planeswalker with toughness-based loyalty abilities")
    void grantsPlaneswalkerAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(com.github.laxika.magicalvibes.model.ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to the enchanted planeswalker reduces toughness instead of loyalty counters")
    void damageReducesToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 2, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(creature.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY)).isZero();
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new Planeswalkerificate());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
