package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OgreMenialTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ogre Menial has activated pump ability")
    void hasActivatedPumpAbility() {
        OgreMenial card = new OgreMenial();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().effects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);
    }

    // ===== Pump ability =====

    @Test
    @DisplayName("Activating pump ability gives +1/+0 until end of turn")
    void pumpGivesPlusOneZero() {
        Permanent perm = new Permanent(new OgreMenial());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getBoostPower()).isEqualTo(1);
        assertThat(perm.getBoostToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple pump activations stack")
    void multiplePumpsStack() {
        Permanent perm = new Permanent(new OgreMenial());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getBoostPower()).isEqualTo(3);
    }

    // ===== Infect: pumped combat damage deals poison =====

    @Test
    @DisplayName("Pumped Ogre Menial deals poison counters equal to boosted power")
    void pumpedDealsPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent perm = new Permanent(new OgreMenial());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        // Pump twice to give it 2 power (base 0 + 2)
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        perm.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal boosted power (2)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unpumped Ogre Menial (0 power) deals no poison counters")
    void unpumpedDealsNoPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent perm = new Permanent(new OgreMenial());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}
