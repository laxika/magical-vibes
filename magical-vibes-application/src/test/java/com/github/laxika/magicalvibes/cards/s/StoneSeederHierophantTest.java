package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneSeederHierophant.class, Forest.class, GrizzlyBears.class})
class StoneSeederHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall untaps Stone-Seeder Hierophant")
    void landfallUntapsSelf() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.setSummoningSick(false);
        hierophant.tap();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hierophant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        hierophant.tap();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(hierophant.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability untaps target land")
    void activatedAbilityUntapsTargetLand() {
        harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a creature")
    void activatedAbilityCannotTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new StoneSeederHierophant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
