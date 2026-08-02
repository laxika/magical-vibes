package com.github.laxika.magicalvibes.cards.p;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolymorphistsJestTest extends BaseCardTest {

    @Test
    @DisplayName("Turns every creature target player controls into a blue 1/1 Frog without abilities")
    void transformsTargetPlayersCreatures() {
        Permanent targetAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent targetBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetFountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castPolymorphistsJest(player2.getId());

        assertThat(targetAngel.getEffectivePower()).isEqualTo(1);
        assertThat(targetAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, targetAngel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, targetAngel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, targetAngel, CardColor.WHITE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(targetAngel, CardSubtype.FROG)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(targetAngel, CardSubtype.ANGEL)).isFalse();

        assertThat(targetBears.getEffectivePower()).isEqualTo(1);
        assertThat(targetBears.getEffectiveToughness()).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(targetBears, CardSubtype.FROG)).isTrue();

        assertThat(targetFountain.getEffectiveColor()).isNull();
        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, ownAngel, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("The transformation wears off at end of turn")
    void transformationWearsOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castPolymorphistsJest(player2.getId());
        assertThat(angel.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.ANGEL)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.FROG)).isFalse();
    }

    private void castPolymorphistsJest(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PolymorphistsJest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
