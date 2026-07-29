package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrimsonRocTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature without flying gives Crimson Roc +1/+0 and first strike")
    void blocksNonFlyerBoostsAndGrantsFirstStrike() {
        Permanent attacker = addReadyCreature(player1, false);
        attacker.setAttacking(true);
        Permanent roc = addReadyRoc(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isEqualTo(1);
        assertThat(roc.getToughnessModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Blocking a flying creature gives Crimson Roc nothing")
    void blocksFlyerDoesNothing() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent roc = addReadyRoc(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Becoming blocked by a creature without flying does not trigger Crimson Roc")
    void becomesBlockedDoesNothing() {
        Permanent roc = addReadyRoc(player1);
        roc.setAttacking(true);
        addReadyCreature(player2, false).getGrantedKeywords().add(Keyword.REACH);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    private Permanent addReadyRoc(Player player) {
        Permanent permanent = new Permanent(new CrimsonRoc());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, boolean flying) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        if (flying) {
            permanent.getGrantedKeywords().add(Keyword.FLYING);
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
