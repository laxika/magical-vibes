package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.r.Ragemonger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trinisphere.class, CrazedGoblin.class, DarksteelIngot.class})
class TrinisphereTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Trinisphere makes a one-mana spell cost three")
    void untappedTrinisphereMakesCheapSpellCostThree() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.castFromHand(player1, new CrazedGoblin(), "{2}{R}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trinisphere does not increase a spell that already costs three mana")
    void doesNotIncreaseThreeManaSpell() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.castFromHand(player1, new DarksteelIngot(), "{3}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapped Trinisphere does not increase spell costs")
    void tappedTrinisphereDoesNotIncreaseSpellCosts() {
        var trinisphere = harness.addToBattlefieldAndReturn(player1, new Trinisphere());
        trinisphere.tap();
        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trinisphere affects spells cast by either player")
    void affectsSpellsCastByOpponent() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CrazedGoblin(), "{2}{R}");

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({Ragemonger.class, FelhideBrawler.class})
    @DisplayName("Trinisphere still raises a spell after a colored mana reduction")
    void coloredManaReductionStillMeetsMinimum() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.addToBattlefield(player1, new Ragemonger());

        assertThatThrownBy(() -> harness.castFromHand(player1, new FelhideBrawler(), "{2}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
