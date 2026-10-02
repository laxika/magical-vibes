package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpriteNoble.class, FledglingMawcor.class, AshcoatBear.class})
class SpriteNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control with flying get +0/+1")
    void staticBuffsOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());

        Permanent sprite = findPermanent(player1, "Fledgling Mawcor");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static buff excludes self, nonflying creatures, and opposing creatures")
    void staticBuffExcludesInvalidCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new AshcoatBear());
        harness.addToBattlefield(player2, new FledglingMawcor());

        Permanent noble = findPermanent(player1, "Sprite Noble");
        Permanent bear = findPermanent(player1, "Ashcoat Bear");
        Permanent opponentSprite = findPermanent(player2, "Fledgling Mawcor");

        assertThat(gqs.getEffectiveToughness(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability gives other flying creatures you control +1/+0 until end of turn")
    void tapAbilityBuffsOtherOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());
        harness.addToBattlefield(player1, new AshcoatBear());
        findPermanent(player1, "Sprite Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Fledgling Mawcor");
        Permanent noble = findPermanent(player1, "Sprite Noble");
        Permanent bear = findPermanent(player1, "Ashcoat Bear");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, noble)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability excludes opposing flying creatures")
    void tapAbilityExcludesOpposingFlyingCreatures() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());
        harness.addToBattlefield(player2, new FledglingMawcor());
        findPermanent(player1, "Sprite Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentSprite = findPermanent(player2, "Fledgling Mawcor");
        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boost wears off at end of turn")
    void tapAbilityBoostWearsOff() {
        harness.addToBattlefield(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());
        findPermanent(player1, "Sprite Noble").setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Fledgling Mawcor");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(3);

        advanceToNextTurn();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(3);
    }

    private void advanceToNextTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UNTAP);
    }
}
