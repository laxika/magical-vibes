package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({TrostaniThreeWhispers.class, RagingGoblin.class, ImplementsOfSacrifice.class})
class TrostaniThreeWhispersTest extends BaseCardTest {

    @Test
    @DisplayName("The abilities grant their respective keywords until end of turn")
    void grantsKeywordsUntilEndOfTurn() {
        addTrostani();
        Permanent target = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The hybrid ability can be paid with either color")
    void hybridAbilityUsesGreenOrWhiteMana() {
        addTrostani();
        Permanent greenTarget = addCreatureReady(player1, new RagingGoblin());
        Permanent whiteTarget = addCreatureReady(player1, new RagingGoblin());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, greenTarget.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, whiteTarget.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, greenTarget, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, whiteTarget, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The abilities cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addTrostani();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addTrostani() {
        Permanent trostani = harness.addToBattlefieldAndReturn(player1, new TrostaniThreeWhispers());
        trostani.setSummoningSick(false);
    }
}
