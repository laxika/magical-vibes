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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        addCreatureReady(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());
        harness.addToBattlefield(player1, new AshcoatBear());

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
        addCreatureReady(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());
        harness.addToBattlefield(player2, new FledglingMawcor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentSprite = findPermanent(player2, "Fledgling Mawcor");
        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T} ability boost wears off at end of turn")
    void tapAbilityBoostWearsOff() {
        addCreatureReady(player1, new SpriteNoble());
        harness.addToBattlefield(player1, new FledglingMawcor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sprite = findPermanent(player1, "Fledgling Mawcor");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(3);

        advanceToNextTurn();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Nobles buff each other and stack their static bonuses")
    void multipleNoblesBuffEachOther() {
        Permanent first = addCreatureReady(player1, new SpriteNoble());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SpriteNoble());
        Permanent mawcor = harness.addToBattlefieldAndReturn(player1, new FledglingMawcor());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mawcor)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mawcor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flying creatures entering before resolution receive the activated boost")
    void tapAbilityUsesCreaturesPresentAtResolution() {
        Permanent noble = addCreatureReady(player1, new SpriteNoble());

        harness.activateAbility(player1, 0, null, null);

        assertThat(noble.isTapped()).isTrue();
        Permanent mawcor = harness.enterBattlefieldAndReturn(player1, new FledglingMawcor());
        assertThat(gqs.getEffectivePower(gd, mawcor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mawcor)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mawcor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mawcor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flying creatures entering after resolution receive only the static bonus")
    void tapAbilityDoesNotBoostLaterEntrants() {
        addCreatureReady(player1, new SpriteNoble());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new FledglingMawcor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new FledglingMawcor());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(3);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new SpriteNoble());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(noble.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Noble cannot activate its tap ability again")
    void cannotActivateAgainWhileTapped() {
        Permanent noble = addCreatureReady(player1, new SpriteNoble());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(noble.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after the Noble dies, while its static bonus ends")
    void tapAbilityResolvesAfterSourceDies() {
        Permanent noble = addCreatureReady(player1, new SpriteNoble());
        Permanent mawcor = harness.addToBattlefieldAndReturn(player1, new FledglingMawcor());
        addCreatureReady(player2, new FledglingMawcor());
        addCreatureReady(player2, new FledglingMawcor());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player2, 0, null, noble.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 1, null, noble.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sprite Noble");
        harness.assertInGraveyard(player1, "Sprite Noble");
        assertThat(gqs.getEffectiveToughness(gd, mawcor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, mawcor)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mawcor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mawcor)).isEqualTo(2);
    }

    private void advanceToNextTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UNTAP);
    }
}
