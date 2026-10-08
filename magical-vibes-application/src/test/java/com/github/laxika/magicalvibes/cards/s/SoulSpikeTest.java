package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.c.ChillToTheBone;
import com.github.laxika.magicalvibes.cards.g.GutlessGhoul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulSpike.class, BorealGriffin.class, ChillToTheBone.class, GutlessGhoul.class})
class SoulSpikeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player and controller gains 4 life")
    void dealsDamageAndGainsLife() {
        harness.setHand(player1, List.of(new SoulSpike()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature and controller gains 4 life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new BorealGriffin());
        harness.setHand(player1, List.of(new SoulSpike()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        UUID targetId = harness.getPermanentId(player2, "Boreal Griffin");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Boreal Griffin");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Can be cast by exiling two black cards from hand instead of paying mana")
    void castsWithTwoBlackCardsExiledFromHand() {
        harness.setHand(player1, List.of(new SoulSpike(), new ChillToTheBone(), new GutlessGhoul()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires two black cards from hand")
    void alternateCostRequiresTwoBlackCards() {
        harness.setHand(player1, List.of(new SoulSpike(), new ChillToTheBone(), new BorealGriffin()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, player2.getId(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller, dealing damage before gaining life")
    void canTargetController() {
        harness.setHand(player1, List.of(new SoulSpike()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Soul Spike");
    }

    @Test
    @DisplayName("Does not gain life when the only target has left the battlefield")
    void doesNotGainLifeWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new BorealGriffin());
        harness.setHand(player1, List.of(new SoulSpike(), new SoulSpike()));
        harness.addMana(player1, ManaColor.BLACK, 14);
        UUID targetId = harness.getPermanentId(player2, "Boreal Griffin");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boreal Griffin");
        harness.assertLife(player1, 24);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof SoulSpike).hasSize(2);
    }

    @Test
    @DisplayName("Alternate cost cannot exile the spell being cast")
    void cannotExileItselfForAlternateCost() {
        harness.setHand(player1, List.of(new SoulSpike(), new ChillToTheBone()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, player2.getId(), List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost cannot exile the same card twice")
    void cannotExileSameCardTwice() {
        harness.setHand(player1, List.of(new SoulSpike(), new ChillToTheBone()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, player2.getId(), List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost uses original hand indices when the spell is between payment cards")
    void alternateCostWorksWithSpellBetweenPaymentCards() {
        harness.setHand(player1, List.of(new ChillToTheBone(), new SoulSpike(), new GutlessGhoul()));

        harness.castInstantWithAlternateExileFromHand(player1, 1, player2.getId(), List.of(2, 0));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Chill to the Bone", "Gutless Ghoul");
        harness.assertInGraveyard(player1, "Soul Spike");
    }
}
