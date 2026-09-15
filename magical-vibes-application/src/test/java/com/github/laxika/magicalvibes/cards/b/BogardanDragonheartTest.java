package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogardanDragonheart.class, GrizzlyBears.class})
class BogardanDragonheartTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature turns it into a 4/4 Dragon with flying and haste")
    void sacrificeAnotherCreatureTransformsDragonheart() {
        Permanent dragonheart = addReadyDragonheart(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart))
                .containsExactly(CardSubtype.DRAGON);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Dragon transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent dragonheart = addReadyDragonheart(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart))
                .containsExactly(CardSubtype.HUMAN, CardSubtype.SHAMAN);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addReadyDragonheart(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDragonheart(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BogardanDragonheart());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
