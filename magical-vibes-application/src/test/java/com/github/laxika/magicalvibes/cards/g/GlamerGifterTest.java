package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlamerGifterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sets another target creature to base 4/4 and grants all creature types")
    void etbSetsBaseStatsAndGrantsAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        UUID bearsId = bears.getId();
        harness.castCreature(player1, 0, List.of(bearsId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("ETB effects wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("ETB can resolve without a target when no other creature exists")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
