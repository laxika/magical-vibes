package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KelpieGuide.class, Forest.class})
class KelpieGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another permanent you control")
    void untapsAnotherPermanentYouControl() {
        Permanent kelpieGuide = addReadyKelpieGuide();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(kelpieGuide.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability cannot target the Kelpie Guide itself or an opposing permanent")
    void untapAbilityRequiresAnotherPermanentYouControl() {
        Permanent kelpieGuide = addReadyKelpieGuide();
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new KelpieGuide());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kelpieGuide.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps a target permanent when its controller has eight lands")
    void tapsTargetPermanentWithEightLands() {
        addReadyKelpieGuide();
        addLands(8);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KelpieGuide());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability requires eight lands")
    void tapAbilityRequiresEightLands() {
        addReadyKelpieGuide();
        addLands(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KelpieGuide());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapsAnOpposingLandAndPaysTapCostImmediately() {
        Permanent source = addReadyKelpieGuide();
        addLands(8);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapAbilityStillResolvesAfterLosingEighthLand() {
        addReadyKelpieGuide();
        addLands(7);
        Permanent eighthLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(eighthLand);
        gd.playerGraveyards.get(player1.getId()).add(eighthLand.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void opposingLandsDoNotSatisfyActivationRestriction() {
        Permanent source = addReadyKelpieGuide();
        addLands(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void tapAbilityCanTargetItsOwnSource() {
        Permanent source = addReadyKelpieGuide();
        addLands(8);

        harness.activateAbility(player1, 0, 1, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void bothAbilitiesRequireAnUntappedSource() {
        Permanent source = addReadyKelpieGuide();
        addLands(8);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothAbilitiesArePreventedBySummoningSickness() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KelpieGuide());
        source.setSummoningSick(true);
        addLands(8);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void untapAbilityDoesNotResolveAfterTargetChangesController() {
        Permanent source = addReadyKelpieGuide();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addReadyKelpieGuide() {
        Permanent kelpieGuide = harness.addToBattlefieldAndReturn(player1, new KelpieGuide());
        kelpieGuide.setSummoningSick(false);
        return kelpieGuide;
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
