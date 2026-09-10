package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhangFeiFierceWarrior.class, ForestBear.class})
class ZhangFeiFierceWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Horsemanship: Zhang Fei can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        Permanent zhangFei = addCreatureReady(player1, new ZhangFeiFierceWarrior());
        zhangFei.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(zhangFei);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship: Zhang Fei can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ZhangFeiFierceWarrior());

        Permanent zhangFei = addCreatureReady(player1, new ZhangFeiFierceWarrior());
        zhangFei.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(zhangFei);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Zhang Fei does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent zhangFei = addCreatureReady(player1, new ZhangFeiFierceWarrior());

        declareAttackers(List.of(0));

        assertThat(zhangFei.isTapped()).isFalse();
    }
}
