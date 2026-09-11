package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WeiEliteCompanions;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaoistMystic.class, WeiEliteCompanions.class, WeiInfantry.class})
class TaoistMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Taoist Mystic can't be blocked by a creature with horsemanship")
    void cannotBeBlockedByHorsemanshipCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new WeiEliteCompanions());

        Permanent atkPerm = addCreatureReady(player1, new TaoistMystic());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taoist Mystic can be blocked by a creature without horsemanship")
    void canBeBlockedByNonHorsemanshipCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new WeiInfantry());

        Permanent atkPerm = addCreatureReady(player1, new TaoistMystic());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
