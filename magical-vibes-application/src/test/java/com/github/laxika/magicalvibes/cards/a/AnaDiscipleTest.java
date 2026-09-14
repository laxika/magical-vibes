package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CetaSanctuary;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnaDisciple.class, AngelfireCrusader.class, CetaSanctuary.class})
class AnaDiscipleTest extends BaseCardTest {

    @Test
    void givesTargetCreatureFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void weakensTargetCreatureUntilEndOfTurn() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void abilitiesCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player2, new CetaSanctuary());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sanctuary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
