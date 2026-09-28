package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNeonate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskLegionSergeant.class, VampireNeonate.class, GrizzlyBears.class})
class DuskLegionSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it grants temporary persist to nontoken Vampires you control")
    void grantsPersistToNontokenVampiresUntilEndOfTurn() {
        Permanent sergeant = addCreatureReady(player1, new DuskLegionSergeant());
        Permanent vampire = addCreatureReady(player1, new VampireNeonate());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sergeant);
        assertThat(vampire.hasKeyword(Keyword.PERSIST)).isTrue();
        assertThat(bear.hasKeyword(Keyword.PERSIST)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.hasKeyword(Keyword.PERSIST)).isFalse();
    }
}
