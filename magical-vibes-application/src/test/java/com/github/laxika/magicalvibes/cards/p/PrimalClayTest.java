package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MorningtidesLight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalClayTest extends BaseCardTest {

    private Permanent castAndReturn(String chosenForm) {
        harness.setHand(player1, List.of(new PrimalClay()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        if (chosenForm != null) {
            harness.handleListChoice(player1, chosenForm);
        }
        return findPermanent(player1, "Primal Clay");
    }

    @Test
    @DisplayName("Resolving awaits a shape choice")
    void resolvingAwaitsShapeChoice() {
        castAndReturn(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing the 3/3 shape sets a 3/3 with no flying or defender")
    void threeThreeShape() {
        Permanent clay = castAndReturn("THREE_THREE");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(clay, CardSubtype.WALL)).isFalse();
    }

    @Test
    @DisplayName("Choosing the 2/2 flying shape sets a 2/2 with flying")
    void twoTwoFlyingShape() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Choosing the 1/6 Wall shape sets a 1/6 Wall with defender")
    void oneSixWallShape() {
        Permanent clay = castAndReturn("ONE_SIX_WALL");

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(clay, CardSubtype.WALL)).isTrue();
    }

    @Test
    @DisplayName("The chosen shape's P/T and keyword survive an end-of-turn reset")
    void chosenShapePersistsAcrossTurns() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        // Model the cleanup-step reset of until-end-of-turn modifiers; the chosen shape is a
        // permanent characteristic and must remain (unlike transient grantedKeywords).
        clay.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, clay)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clay)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, clay, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Returning from exile asks for a new shape")
    void returningFromExileAsksForNewShape() {
        Permanent clay = castAndReturn("TWO_TWO_FLYING");

        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(clay.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ONE_SIX_WALL");

        Permanent returnedClay = findPermanent(player1, "Primal Clay");
        assertThat(gqs.getEffectivePower(gd, returnedClay)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returnedClay)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, returnedClay, Keyword.DEFENDER)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(returnedClay, CardSubtype.WALL)).isTrue();
    }
}
