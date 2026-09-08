package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BriarPatchTest extends BaseCardTest {

    private void addPatch() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new BriarPatch()));
    }

    private Permanent addAttacker(UUID attackTarget) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Creatures attacking its controller get -1/-0")
    void weakensCreaturesAttackingController() {
        addPatch();
        Permanent attacker = addAttacker(player1.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures attacking another player are unaffected")
    void ignoresCreaturesAttackingSomeoneElse() {
        addPatch();
        Permanent attacker = addAttacker(player2.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that are not attacking are unaffected")
    void ignoresNonAttackingCreatures() {
        addPatch();
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
