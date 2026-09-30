package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.b.BarrenGlory;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VensersDiffusion.class, AncestralVision.class, BarrenGlory.class, HorizonCanopy.class})
class VensersDiffusionTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        harness.addToBattlefieldAndReturn(player2, new BarrenGlory());
        Card spell = new VensersDiffusion();
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Barren Glory"));

        harness.assertNotOnBattlefield(player2, "Barren Glory");
        harness.assertInHand(player2, "Barren Glory");
    }

    @Test
    void returnsStolenTargetToItsOwnersHand() {
        var target = harness.addToBattlefieldAndReturn(player2, new BarrenGlory());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Barren Glory");
        harness.assertNotInHand(player2, "Barren Glory");
    }

    @Test
    void returnsSuspendedCardToItsOwnersHand() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 2);
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.findExiledCard(target.getId())).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
    }

    @Test
    void cannotTargetExiledCardWithNonSuspendTimeCounters() {
        BarrenGlory target = new BarrenGlory();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 2);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefieldAndReturn(player2, new HorizonCanopy());
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Horizon Canopy")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetUnsuspendedExiledCard() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
