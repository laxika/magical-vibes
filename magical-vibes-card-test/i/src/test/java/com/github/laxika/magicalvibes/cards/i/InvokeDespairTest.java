package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GenerousVisitor;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.k.KaitoShizuki;
import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.cards.r.RoaringEarth;
import com.github.laxika.magicalvibes.cards.y.YasharnImplacableEarth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeDespair.class, GenerousVisitor.class, NetworkDisruptor.class, KaitoShizuki.class,
        RoaringEarth.class, JukaiPreserver.class, YasharnImplacableEarth.class})
class InvokeDespairTest extends BaseCardTest {

    @Test
    @DisplayName("The target opponent sacrifices a creature, enchantment, and planeswalker")
    void sacrificesEachPermanentType() {
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        harness.addToBattlefield(player2, new NetworkDisruptor());
        harness.addToBattlefield(player2, new RoaringEarth());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KaitoShizuki());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, chosenCreature.getId());

        harness.assertOnBattlefield(player2, "Network Disruptor");
        harness.assertNotOnBattlefield(player2, "Generous Visitor");
        harness.assertNotOnBattlefield(player2, "Roaring Earth");
        harness.assertNotOnBattlefield(player2, "Kaito Shizuki");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Generous Visitor", "Roaring Earth", "Kaito Shizuki");
    }

    @Test
    @DisplayName("The target opponent loses life and the controller draws when types are unavailable")
    void fallbackAppliesForUnavailableTypes() {
        harness.addToBattlefield(player2, new NetworkDisruptor());
        harness.setLibrary(player1, List.of(new GenerousVisitor(), new NetworkDisruptor()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Network Disruptor");
    }

    @Test
    @DisplayName("Invoke Despair can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new InvokeDespair()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An opponent with no permanents loses six life and the caster draws three cards")
    void allUnavailableTypesApplyFallback() {
        harness.setLibrary(player1, List.of(new InvokeDespair(), new InvokeDespair(), new InvokeDespair()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        harness.assertLife(player2, lifeBefore - 6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The enchantment and planeswalker sacrifices still occur after the creature fallback")
    void missingCreatureDoesNotSkipLaterSacrifices() {
        harness.addToBattlefield(player2, new RoaringEarth());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KaitoShizuki());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new InvokeDespair()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Roaring Earth");
        harness.assertInGraveyard(player2, "Kaito Shizuki");
    }

    @Test
    @DisplayName("An enchantment creature sacrificed first cannot also satisfy the enchantment sacrifice")
    void enchantmentCreatureCannotBeSacrificedTwice() {
        harness.addToBattlefield(player2, new JukaiPreserver());
        harness.setLibrary(player1, List.of(new InvokeDespair(), new InvokeDespair()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        harness.assertInGraveyard(player2, "Jukai Preserver");
        harness.assertLife(player2, lifeBefore - 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Yasharn does not prevent sacrifices made while Invoke Despair resolves")
    void costSacrificeRestrictionDoesNotPreventEffectSacrifices() {
        harness.addToBattlefield(player1, new YasharnImplacableEarth());
        harness.addToBattlefield(player2, new GenerousVisitor());
        harness.addToBattlefield(player2, new RoaringEarth());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KaitoShizuki());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new InvokeDespair(), new InvokeDespair(), new InvokeDespair()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castAndResolveInvokeDespair(player2.getId());

        harness.assertInGraveyard(player2, "Generous Visitor");
        harness.assertInGraveyard(player2, "Roaring Earth");
        harness.assertInGraveyard(player2, "Kaito Shizuki");
        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castAndResolveInvokeDespair(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new InvokeDespair()));
        addMana(player1);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 4);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
