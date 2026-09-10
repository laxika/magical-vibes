package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpoilsOfAdventure.class, BoggartBrute.class, FaerieMiscreant.class, Forest.class,
        FugitiveWizard.class, SoulWarden.class})
class SpoilsOfAdventureTest extends BaseCardTest {

    @Test
    @DisplayName("Costs four less with a full party")
    void costsFourLessWithFullParty() {
        addFullParty(player1);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Gains three life and draws three cards")
    void gainsLifeAndDrawsCards() {
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Does not count an opponent's party")
    void doesNotCountOpponentsParty() {
        addFullParty(player2);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addFullParty(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new SoulWarden());
        harness.addToBattlefield(player, new FaerieMiscreant());
        harness.addToBattlefield(player, new BoggartBrute());
        harness.addToBattlefield(player, new FugitiveWizard());
    }
}
