package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialRay.class, GrizzlyBears.class, Shock.class, LavaSpike.class, ReachThroughMists.class})
class GlacialRayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsTwoDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void dealsTwoDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        GlacialRay ray = new GlacialRay();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneShock, ray));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ray);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock(), new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    @Test
    @DisplayName("Spliced damage can target a different player from the original spell")
    void splicedDamageHasIndependentTarget() {
        GlacialRay ray = new GlacialRay();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LavaSpike(), ray));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.ensurePriority(player1);
        gs.playCardWithSplice(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), player1.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ray);
    }

    @Test
    @DisplayName("Splice adds damage to a spell that originally has no target")
    void splicesOntoUntargetedArcaneSpell() {
        GlacialRay ray = new GlacialRay();
        GlacialRay drawnCard = new GlacialRay();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ReachThroughMists(), ray));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(ray, drawnCard);
    }

    @Test
    @DisplayName("Cannot splice the same card onto one spell twice")
    void cannotSpliceSameCardTwice() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LavaSpike(), new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting the host spell requires enough mana for its splice cost too")
    void cannotSpliceWithoutEnoughMana() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LavaSpike(), new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
