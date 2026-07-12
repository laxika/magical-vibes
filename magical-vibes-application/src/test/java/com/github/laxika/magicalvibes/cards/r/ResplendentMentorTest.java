package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResplendentMentorTest extends BaseCardTest {

    @Test
    @DisplayName("White creature you control gains the tap-for-life ability")
    void whiteCreatureGainsLifeAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        lion.setSummoningSick(false);

        int lionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lion);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, lionIndex, null, null);
        harness.passBothPriorities();

        assertThat(lion.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-white creature does not gain the granted ability")
    void nonWhiteCreatureDoesNotGainAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> harness.activateAbility(player1, bearsIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponent's white creatures do not gain the granted ability")
    void opponentWhiteCreatureDoesNotGainAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent lion = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        lion.setSummoningSick(false);

        int lionIndex = gd.playerBattlefields.get(player2.getId()).indexOf(lion);

        assertThatThrownBy(() -> harness.activateAbility(player2, lionIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability is lost when Resplendent Mentor leaves the battlefield")
    void abilityLostWhenMentorLeaves() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        lion.setSummoningSick(false);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Resplendent Mentor"));

        int lionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lion);

        assertThatThrownBy(() -> harness.activateAbility(player1, lionIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
