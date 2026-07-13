package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadjanSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses flying until end of turn")
    void targetLosesFlyingUntilEndOfTurn() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RadjanSpirit());
        spirit.setSummoningSick(false);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature without flying (no effect on it)")
    void targetsNonFlyer() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RadjanSpirit());
        spirit.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
