package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({MerfolkTunnelGuide.class, MerfolkSpy.class, GrizzlyBears.class, MerrowCommerce.class, Forest.class})
class MerfolkTunnelGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land puts it into hand without adding a counter")
    void exploreLandGoesToHand() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        Forest revealed = new Forest();
        harness.setLibrary(player1, List.of(revealed));

        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature Merfolk entering also triggers the boon")
    void noncreatureMerfolkEntryTriggersBoon() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerrowCommerce());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Merfolk neither triggers nor consumes the boon")
    void opposingMerfolkDoesNotConsumeBoon() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new MerfolkSpy());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        for (int i = 0; i < 3; i++) {
            harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, guide.getId());
            harness.passBothPriorities();
        }

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.boons).isEmpty();
    }

    @Test
    @DisplayName("Exploring can put the revealed nonland into the graveyard")
    void exploreCanGraveyardNonland() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        MerfolkTunnelGuide revealed = new MerfolkTunnelGuide();
        harness.setLibrary(player1, List.of(revealed));

        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
    }

    @Test
    @DisplayName("The boon cannot target an opponent's Merfolk")
    void cannotTargetOpposingMerfolk() {
        Permanent opposingMerfolk = harness.enterBattlefieldAndReturn(player2, new MerfolkSpy());
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new MerfolkTunnelGuide());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingMerfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

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
