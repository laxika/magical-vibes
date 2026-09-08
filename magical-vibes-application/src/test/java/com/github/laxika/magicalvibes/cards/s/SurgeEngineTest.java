package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurgeEngineTest extends BaseCardTest {

    @Test
    void losesDefenderAndBecomesUnblockableIndefinitely() {
        Permanent engine = addCreatureReady(player1, new SurgeEngine());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(engine.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, engine)).isTrue();
    }

    @Test
    void becomesBlueAndSetsBasePowerAndToughnessIndefinitely() {
        Permanent engine = addCreatureReady(player1, new SurgeEngine());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not have defender");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, engine)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, engine)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(4);
    }

    @Test
    void drawsThreeCardsOnlyOnceAndRequiresBlue() {
        Permanent engine = addCreatureReady(player1, new SurgeEngine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blue");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.BLUE, 6);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activated only once");
    }
}
