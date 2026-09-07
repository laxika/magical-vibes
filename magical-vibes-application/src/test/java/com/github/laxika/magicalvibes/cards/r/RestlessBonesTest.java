package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestlessBones.class, GrizzlyBears.class, Forest.class})
class RestlessBonesTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants swampwalk to target creature until end of turn")
    void grantsSwampwalkUntilEndOfTurn() {
        Permanent bones = addCreatureReady(player1, new RestlessBones());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bones.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("The second ability gives Restless Bones a regeneration shield")
    void regeneratesThisCreature() {
        Permanent bones = addCreatureReady(player1, new RestlessBones());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bones.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new RestlessBones());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
