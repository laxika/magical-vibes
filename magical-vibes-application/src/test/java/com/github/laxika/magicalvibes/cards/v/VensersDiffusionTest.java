package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VensersDiffusion.class, AncestralVision.class, GrizzlyBears.class, Island.class})
class VensersDiffusionTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new VensersDiffusion();
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castInstant(player1, 0, findPermanent(player2, "Grizzly Bears").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void returnsSuspendedCardToItsOwnersHand() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 2);
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getId())).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new VensersDiffusion()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                findPermanent(player2, "Island").getId()))
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
