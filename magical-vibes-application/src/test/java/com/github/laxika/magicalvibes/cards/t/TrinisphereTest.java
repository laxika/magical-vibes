package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.g.GitaxianProbe;
import com.github.laxika.magicalvibes.cards.w.WhispersOfTheMuse;
import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.r.Ragemonger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trinisphere.class, CrazedGoblin.class, DarksteelIngot.class, Ragemonger.class,
        FelhideBrawler.class, DeepAnalysis.class, WhispersOfTheMuse.class, GitaxianProbe.class})
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

    @Test
    @DisplayName("Multiple untapped Trinispheres do not raise the minimum above three")
    void multipleTrinispheresDoNotStack() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.addToBattlefield(player2, new Trinisphere());

        harness.castFromHand(player1, new CrazedGoblin(), "{2}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Additional generic mana imposed by Trinisphere may be paid with any color")
    void minimumMayBePaidWithColoredMana() {
        harness.addToBattlefield(player1, new Trinisphere());

        harness.castFromHand(player1, new CrazedGoblin(), "{U}{U}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trinisphere prevents a cheap spell being cast with only its printed cost")
    void cannotCastWithOnlyPrintedManaCost() {
        harness.addToBattlefield(player1, new Trinisphere());

        assertThatThrownBy(() -> harness.castFromHand(player1, new CrazedGoblin(), "{R}"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Crazed Goblin");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Trinisphere raises Deep Analysis's flashback mana cost to three")
    void minimumUsesFlashbackCostInsteadOfPrintedCost() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.setGraveyard(player1, List.of(new DeepAnalysis()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Trinisphere includes buyback mana when determining the total cost")
    void buybackManaCountsTowardMinimum() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.setHand(player1, List.of(new WhispersOfTheMuse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithBuyback(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Phyrexian mana paid with life does not count toward Trinisphere's minimum")
    void phyrexianLifePaymentStillRequiresThreeMana() {
        harness.addToBattlefield(player1, new Trinisphere());
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
