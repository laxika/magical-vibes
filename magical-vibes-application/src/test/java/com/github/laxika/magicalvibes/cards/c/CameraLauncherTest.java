package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SpinOut;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Camera Launcher")
@CardUsed({CameraLauncher.class, SpinOut.class})
class CameraLauncherTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts a counter on Camera Launcher and creates a flying Thopter")
    void exhaustAbilityPutsCounterAndCreatesThopter() {
        Permanent launcher = addLauncher();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Thopter");
        assertThat(launcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addLauncher();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust can be activated while tapped and summoning sick")
    void exhaustDoesNotRequireTappingOrHaste() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new CameraLauncher());
        launcher.setSummoningSick(true);
        launcher.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(launcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(launcher.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    @DisplayName("Exhaust is used up as soon as it is activated")
    void exhaustCannotBeActivatedAgainBeforeResolution() {
        Permanent launcher = addLauncher();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(launcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Thopter")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();
        assertThat(launcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Camera Launcher has its own exhaust activation limit")
    void exhaustLimitIsIndependentForEachPermanent() {
        Permanent first = addLauncher();
        Permanent second = addCreatureReady(player1, new CameraLauncher());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(2);
    }

    private Permanent addLauncher() {
        return addCreatureReady(player1, new CameraLauncher());
    }

    @Test
    @DisplayName("Exhaust still creates the Thopter when Camera Launcher is destroyed in response")
    void exhaustCreatesTokenAfterSourceLeavesBattlefield() {
        Permanent launcher = addLauncher();
        harness.setHand(player1, List.of(new SpinOut()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player1, 0, launcher.getId());

        harness.assertInGraveyard(player1, "Camera Launcher");
        harness.assertNotOnBattlefield(player1, "Camera Launcher");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }
}
