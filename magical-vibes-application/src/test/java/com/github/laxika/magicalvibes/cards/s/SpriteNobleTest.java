package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpriteNoble.class, CloudSprite.class, GrizzlyBears.class})
class SpriteNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control with flying get +0/+1")
    void staticBuffsOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Static buff excludes self, nonflying creatures, and opposing creatures")
    void staticBuffExcludesInvalidCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent noble = findPermanent(player1, "Sprite Noble");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentSprite = findPermanent(player2, "Cloud Sprite");

        assertThat(gqs.getEffectiveToughness(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(1);
    }

    @Test
    @DisplayName("{T} ability gives other flying creatures you control +1/+0 until end of turn")
    void tapAbilityBuffsOtherOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new CloudSprite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Sprite Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Cloud Sprite");
        Permanent noble = findPermanent(player1, "Sprite Noble");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boost wears off at end of turn")
    void tapAbilityBoostWearsOff() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new CloudSprite());
        findPermanent(player1, "Sprite Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Cloud Sprite");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);

        advanceToNextTurn();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    private void advanceToNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
