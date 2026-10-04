package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedronBlade.class, GrizzlyBears.class, Ornithopter.class})
class HedronBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature gains deathtouch when blocked by a colorless creature")
    void gainsDeathtouchWhenBlockedByColorlessCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Ornithopter());

        declareBlockers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does not gain deathtouch when blocked by a colored creature")
    void doesNotGainDeathtouchWhenBlockedByColoredCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Hedron Blade's temporary deathtouch expires at end of turn")
    void deathtouchExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Ornithopter());

        declareBlockers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    private void declareBlockers() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }

    private Permanent addBladeReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent blade = new Permanent(new HedronBlade());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blade);
        return blade;
    }
}
