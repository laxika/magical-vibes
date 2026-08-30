package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZodiacRabbit;
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

@CardUsed({ScarwoodHag.class, Forest.class, GrizzlyBears.class, ZodiacRabbit.class})
class ScarwoodHagTest extends BaseCardTest {

    @Test
    @DisplayName("Four green mana and tapping grants forestwalk to a target creature")
    void grantsForestwalkUntilEndOfTurn() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Tapping removes forestwalk from a target creature until end of turn")
    void removesForestwalkUntilEndOfTurn() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent rabbit = addCreatureReady(player2, new ZodiacRabbit());

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isTrue();

        harness.activateAbility(player1, 0, 1, null, rabbit.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Both abilities can target only creatures")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
