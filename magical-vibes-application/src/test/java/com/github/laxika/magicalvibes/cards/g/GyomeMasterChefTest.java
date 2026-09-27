package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JunglebornPioneer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GyomeMasterChef.class, GrizzlyBears.class, JunglebornPioneer.class, Forest.class})
class GyomeMasterChefTest extends BaseCardTest {

    @Test
    void createsFoodForNontokenCreaturesButNotCreatureTokens() {
        harness.addToBattlefield(player1, new GyomeMasterChef());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new JunglebornPioneer());
        harness.passBothPriorities();

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    void sacrificesFoodToGrantTargetCreatureIndestructibleAndTapIt() {
        Permanent gyome = harness.addToBattlefieldAndReturn(player1, new GyomeMasterChef());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveControllerEndStep();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gyome), null, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityCannotTargetALand() {
        Permanent gyome = harness.addToBattlefieldAndReturn(player1, new GyomeMasterChef());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveControllerEndStep();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(gyome), null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
