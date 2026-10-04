package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantsIre.class, BlindSpotGiant.class, ChandraNalaar.class, NamelessInversion.class})
class GiantsIreTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4); // {3}{R}
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Deals 4 damage to the target player")
    void dealsFourDamage() {
        int before = gd.getLife(player2.getId());
        cast();
        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 4);
    }

    @Test
    @DisplayName("Draws a card if you control a Giant")
    void drawsWithGiant() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.setLibrary(player1, List.of(new GiantsIre()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a Giant")
    void noDrawWithoutGiant() {
        harness.setLibrary(player1, List.of(new GiantsIre()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Giant does not enable the draw")
    void opposingGiantDoesNotEnableDraw() {
        harness.addToBattlefield(player2, new BlindSpotGiant());
        harness.setLibrary(player1, List.of(new GiantsIre()));

        cast();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Giants still draw only one card")
    void multipleGiantsDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.setLibrary(player1, List.of(new GiantsIre(), new GiantsIre()));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage to a planeswalker and still draws with a Giant")
    void damagesPlaneswalkerAndDraws() {
        harness.addToBattlefield(player2, new ChandraNalaar());
        var chandra = findPermanent(player2, "Chandra Nalaar");
        int loyaltyBefore = chandra.getCounterCount(CounterType.LOYALTY);
        harness.addToBattlefield(player1, new BlindSpotGiant());
        harness.setLibrary(player1, List.of(new GiantsIre()));
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 4);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BlindSpotGiant());
        var giant = findPermanent(player2, "Blind-Spot Giant");
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the last Giant before resolution prevents the draw")
    void checksGiantAtResolution() {
        harness.addToBattlefield(player1, new BlindSpotGiant());
        var giant = findPermanent(player1, "Blind-Spot Giant");
        harness.setLibrary(player1, List.of(new GiantsIre()));
        harness.setHand(player1, List.of(new GiantsIre()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
