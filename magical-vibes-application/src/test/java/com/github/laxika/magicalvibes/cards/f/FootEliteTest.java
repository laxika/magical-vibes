package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FootElite.class, GrizzlyBears.class})
class FootEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts another creature you control and grants indestructible")
    void attackingBoostsAndProtectsAnotherCreature() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, footElite, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target Foot Elite itself")
    void cannotTargetItself() {
        Permanent footElite = addCreatureReady(player1, new FootElite());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, footElite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
