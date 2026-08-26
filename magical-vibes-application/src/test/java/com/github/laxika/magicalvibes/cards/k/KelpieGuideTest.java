package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability requires eight lands")
    void tapAbilityRequiresEightLands() {
        addReadyKelpieGuide();
        addLands(7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKelpieGuide() {
        Permanent kelpieGuide = new Permanent(new KelpieGuide());
        kelpieGuide.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kelpieGuide);
        return kelpieGuide;
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
