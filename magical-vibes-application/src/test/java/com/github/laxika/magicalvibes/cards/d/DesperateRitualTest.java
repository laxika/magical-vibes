package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesperateRitual.class, GlacialRay.class, YamabushisFlame.class})
class DesperateRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving adds three red mana to controller's pool")
    void resolvingAddsThreeRedMana() {
        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
        harness.assertInGraveyard(player1, "Desperate Ritual");
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneRay = new GlacialRay();
        DesperateRitual ritual = new DesperateRitual();
        harness.setHand(player1, List.of(arcaneRay, ritual));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ritual);
        harness.assertInGraveyard(player1, "Glacial Ray");
    }

    @Test
    @DisplayName("Splices multiple copies onto one Arcane spell")
    void splicesMultipleCopiesOntoArcaneSpell() {
        Card arcaneRay = new GlacialRay();
        DesperateRitual firstRitual = new DesperateRitual();
        DesperateRitual secondRitual = new DesperateRitual();
        harness.setHand(player1, List.of(arcaneRay, firstRitual, secondRitual));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstRitual, secondRitual);
        harness.assertInGraveyard(player1, "Glacial Ray");
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        Card nonArcaneSpell = new YamabushisFlame();
        DesperateRitual ritual = new DesperateRitual();
        harness.setHand(player1, List.of(nonArcaneSpell, ritual));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonArcaneSpell, ritual);
    }
}
