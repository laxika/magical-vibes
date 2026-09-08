package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BladeHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control have double strike")
    void attackingCreaturesYouControlHaveDoubleStrike() {
        harness.addToBattlefield(player1, new BladeHistorian());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blade Historian gains double strike while attacking")
    void sourceGainsDoubleStrikeWhileAttacking() {
        BladeHistorian historian = new BladeHistorian();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, historian);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, permanent, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures you control do not have double strike")
    void nonAttackingCreaturesDoNotHaveDoubleStrike() {
        harness.addToBattlefield(player1, new BladeHistorian());
        Permanent bears = addReadyBears(player1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not have double strike")
    void opponentAttackingCreaturesDoNotHaveDoubleStrike() {
        harness.addToBattlefield(player1, new BladeHistorian());
        Permanent bears = addAttackingBears(player2);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Blade Historian leaves the battlefield")
    void doubleStrikeIsLostWhenSourceLeaves() {
        Permanent bears = addAttackingBears(player1);
        harness.addToBattlefield(player1, new BladeHistorian());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof BladeHistorian);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addReadyBears(controller);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addReadyBears(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }
}
