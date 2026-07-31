package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarabOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every Aura attached to the target, including an opponent's, to its owner's hand")
    void returnsAllAttachedAuras() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GameData gd = harness.getGameData();

        Permanent ownAura = new Permanent(new HolyStrength());
        ownAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(ownAura);

        Permanent opponentAura = new Permanent(new GiantStrength());
        opponentAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(opponentAura);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownAura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentAura);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Sacrifices itself and schedules a draw at the next upkeep")
    void sacrificesItselfAndSchedulesDraw() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scarab);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }
}
