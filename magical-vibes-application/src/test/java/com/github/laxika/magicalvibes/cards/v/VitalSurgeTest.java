package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.cards.r.RibbonsOfTheReikai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitalSurge.class, RibbonsOfTheReikai.class, MendingHands.class})
class VitalSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life")
    void gainsThreeLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Vital Surge in hand")
    void splicesOntoArcaneSpell() {
        RibbonsOfTheReikai arcaneHost = new RibbonsOfTheReikai();
        VitalSurge vitalSurge = new VitalSurge();
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(arcaneHost, vitalSurge));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithSplice(player1, 0, null, List.of(1));
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(vitalSurge);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        VitalSurge vitalSurge = new VitalSurge();
        harness.setHand(player1, List.of(new MendingHands(), vitalSurge));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player1.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }
}
