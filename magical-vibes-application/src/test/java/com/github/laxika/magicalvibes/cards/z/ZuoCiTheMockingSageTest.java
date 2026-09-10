package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.p.PoisonArrow;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.cards.w.WeiEliteCompanions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZuoCiTheMockingSage.class, WeiEliteCompanions.class, WeiInfantry.class, PoisonArrow.class})
class ZuoCiTheMockingSageTest extends BaseCardTest {

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Zuo Ci")
    void hexproofPreventsOpponentTargeting() {
        Permanent zuoCi = addCreatureReady(player1, new ZuoCiTheMockingSage());

        harness.setHand(player2, List.of(new PoisonArrow()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, zuoCi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Zuo Ci can't be blocked by a creature with horsemanship")
    void cannotBeBlockedByHorsemanshipCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new WeiEliteCompanions());

        Permanent atkPerm = addCreatureReady(player1, new ZuoCiTheMockingSage());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zuo Ci can be blocked by a creature without horsemanship")
    void canBeBlockedByNonHorsemanshipCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new WeiInfantry());

        Permanent atkPerm = addCreatureReady(player1, new ZuoCiTheMockingSage());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
