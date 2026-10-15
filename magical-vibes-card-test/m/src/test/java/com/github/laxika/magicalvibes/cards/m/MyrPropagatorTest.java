package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrPropagator.class})
class MyrPropagatorTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability creates a token copy of Myr Propagator")
    void activatedAbilityCreatesTokenCopy() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Token copy has the same activated ability as the original")
    void tokenCopyHasSameActivatedAbility() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .findFirst().orElseThrow();

        token.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple activations create multiple tokens")
    void multipleActivationsCreateMultipleTokens() {
        addPropagatorReady(player1);

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Untap source for second activation
        GameData gd = harness.getGameData();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && !p.getCard().isToken())
                .findFirst().orElseThrow();
        source.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Source leaving battlefield before resolution still creates token via last-known info")
    void sourceLeftBattlefieldStillCreatesToken() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        // Remove the source before ability resolves
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Myr Propagator") && !p.getCard().isToken());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken());
    }

    @Test
    void newlyCreatedTokenCannotActivateWhileSummoningSick() {
        addPropagatorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void activationRequiresThreeMana() {
        addPropagatorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void tappedSourceCannotActivateAgain() {
        addPropagatorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void tokenDoesNotCopyCountersOrTappedStatus() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MyrPropagator());
        source.setSummoningSick(false);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(source.isTapped()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addPropagatorReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MyrPropagator());
        perm.setSummoningSick(false);
    }
}
