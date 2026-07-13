package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnseenWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants forestwalk to target creature")
    void grantsForestwalkToTarget() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk wears off at end of turn")
    void forestwalkWearsOff() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new UnseenWalker());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
