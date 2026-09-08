package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanceOfTheSkywise.class, GrizzlyBears.class, SerraAngel.class, FountainOfYouth.class})
class DanceOfTheSkywiseTest extends BaseCardTest {

    @Test
    void transformsTargetCreatureIntoBlueDragonIllusion() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDanceOfTheSkywise(bear);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.DRAGON)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.ILLUSION)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isFalse();
    }

    @Test
    void losesExistingAbilitiesButGainsFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();

        castDanceOfTheSkywise(angel);

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void transformationWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDanceOfTheSkywise(bear);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bear, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.BEAR)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.DRAGON)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(bear, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    void cannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castDanceOfTheSkywise(Permanent target) {
        harness.setHand(player1, List.of(new DanceOfTheSkywise()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
