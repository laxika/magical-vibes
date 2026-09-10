package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MordenkainensPolymorph.class, GrizzlyBears.class, FountainOfYouth.class})
class MordenkainensPolymorphTest extends BaseCardTest {

    @Test
    void transformsTargetCreatureIntoDragonWithBaseFourFourAndFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMordenkainensPolymorph(bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.DRAGON)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isFalse();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMordenkainensPolymorph(bears.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.DRAGON)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MordenkainensPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castMordenkainensPolymorph(UUID targetId) {
        harness.setHand(player1, List.of(new MordenkainensPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
