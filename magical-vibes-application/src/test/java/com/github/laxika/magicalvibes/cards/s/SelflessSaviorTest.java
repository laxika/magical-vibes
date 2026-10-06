package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.FinishingBlow;
import com.github.laxika.magicalvibes.cards.m.Meteorite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelflessSavior.class, AlpineWatchdog.class, FinishingBlow.class, Shock.class, Meteorite.class})
class SelflessSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing grants indestructible to another creature you control")
    void sacrificeGrantsIndestructible() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.assertNotOnBattlefield(player1, "Selfless Savior");
        harness.assertInGraveyard(player1, "Selfless Savior");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Selfless Savior");
    }

    @Test
    @DisplayName("Indestructible protects the target from destruction until end of turn")
    void targetSurvivesDestruction() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself or a creature controlled by an opponent")
    void restrictsActivationTarget() {
        Permanent savior = harness.addToBattlefieldAndReturn(player1, new SelflessSavior());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, savior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        harness.assertOnBattlefield(player1, "Selfless Savior");
        harness.assertNotInGraveyard(player1, "Selfless Savior");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent meteorite = harness.addToBattlefieldAndReturn(player1, new Meteorite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, meteorite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");

        harness.assertOnBattlefield(player1, "Selfless Savior");
        harness.assertNotInGraveyard(player1, "Selfless Savior");
    }

    @Test
    @DisplayName("Can target another Selfless Savior while summoning sick and tapped")
    void canProtectAnotherSaviorWithoutTapping() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SelflessSavior());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SelflessSavior());
        source.setSummoningSick(true);
        source.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Selfless Savior");
    }

    @Test
    @DisplayName("Indestructible protects a creature from lethal damage")
    void targetSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player1, "Alpine Watchdog");
        harness.assertNotInGraveyard(player1, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Removing the target in response does not refund the sacrifice")
    void targetCanDieBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new SelflessSavior());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Selfless Savior");
        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Selfless Savior");
        harness.assertInGraveyard(player1, "Alpine Watchdog");
        assertThat(gd.stack).isEmpty();
    }
}
