package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KozilekButcherOfTruth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingDestiny.class, HillGiant.class, Forest.class, KozilekButcherOfTruth.class})
class LivingDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the mana value of the revealed creature card")
    void gainsLifeEqualToRevealedCreatureManaValue() {
        LivingDestiny spell = new LivingDestiny();
        HillGiant revealed = new HillGiant();
        harness.setHand(player1, List.of(spell, revealed));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        harness.assertInGraveyard(player1, "Living Destiny");
    }

    @Test
    @DisplayName("Cannot be cast without a creature card to reveal")
    void requiresCreatureCardToReveal() {
        harness.setHand(player1, List.of(new LivingDestiny(), new Forest()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reveals the selected creature before the spell's hand position and gains life only on resolution")
    void revealsSelectedCreatureBeforeSpellAndGainsLifeOnResolution() {
        KozilekButcherOfTruth revealed = new KozilekButcherOfTruth();
        HillGiant otherCreature = new HillGiant();
        harness.setHand(player1, List.of(revealed, new LivingDestiny(), otherCreature));
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstantWithDiscard(player1, 1, null, 0);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed, otherCreature);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed, otherCreature);
        harness.assertInGraveyard(player1, "Living Destiny");
    }

    @Test
    @DisplayName("Cannot skip the reveal cost even with a creature card in hand")
    void cannotSkipRevealCost() {
        harness.setHand(player1, List.of(new LivingDestiny(), new KozilekButcherOfTruth()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
