package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkTunnelGuide.class, MerfolkSpy.class, GrizzlyBears.class})
class MerfolkTunnelGuideTest extends BaseCardTest {

    @Test
    @DisplayName("A Merfolk entering lets you choose a Merfolk you control to explore")
    void merfolkEntryExploresChosenMerfolk() {
        Permanent recipient = harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();

        Permanent nonMerfolk = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        Permanent enteringMerfolk = harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The three-time boon expires after three Merfolk entries")
    void boonExpiresAfterThreeMerfolkEntries() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, guide.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        }

        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.boons).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A non-Merfolk entry does not consume the boon")
    void nonMerfolkEntryDoesNotConsumeBoon() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.boons).hasSize(1);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The explore target must be a Merfolk creature you control")
    void cannotTargetNonMerfolk() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        Permanent nonMerfolk = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonMerfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }
}
