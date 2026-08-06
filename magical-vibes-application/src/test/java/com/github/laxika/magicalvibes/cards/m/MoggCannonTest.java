package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoggCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and flying to target creature you control")
    void boostsAndGrantsFlying() {
        Permanent cannon = new Permanent(new MoggCannon());
        gd.playerBattlefields.get(player1.getId()).add(cannon);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(after.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(2);
        assertThat(cannon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target creature is destroyed at the beginning of the next end step")
    void destroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent cannon = new Permanent(new MoggCannon());
        gd.playerBattlefields.get(player1.getId()).add(cannon);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent cannon = new Permanent(new MoggCannon());
        gd.playerBattlefields.get(player1.getId()).add(cannon);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
