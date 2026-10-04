package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GatesOfIstfell.class, Forest.class})
class GatesOfIstfellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for white mana")
    void entersTappedAndTapsForWhite() {
        harness.setHand(player1, List.of(new GatesOfIstfell()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Gates of Istfell");

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays to gain life, draw two cards, and sacrifice itself")
    void gainsLifeDrawsCardsAndSacrificesItself() {
        harness.addToBattlefield(player1, new GatesOfIstfell());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Gates of Istfell");
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before life gain and card draw resolve")
    void paysCostsBeforeResolving() {
        harness.addToBattlefield(player1, new GatesOfIstfell());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Gates of Istfell");
        harness.assertInGraveyard(player1, "Gates of Istfell");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land cannot activate either ability")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new GatesOfIstfell()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Gates of Istfell");
        harness.assertNotInGraveyard(player1, "Gates of Istfell");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Extra generic mana cannot replace the second blue mana")
    void requiresBothBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GatesOfIstfell());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(land.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Gates of Istfell");
        harness.assertNotInGraveyard(player1, "Gates of Istfell");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
