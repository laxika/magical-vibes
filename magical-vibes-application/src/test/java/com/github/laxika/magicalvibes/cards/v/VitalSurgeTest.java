package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class VitalSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life")
    void gainsThreeLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Vital Surge in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneHost = new HolyDay().createRuntimeCopy();
        arcaneHost.setSubtypes(List.of(CardSubtype.ARCANE));
        VitalSurge vitalSurge = new VitalSurge();
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(arcaneHost, vitalSurge));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, null, List.of(1));
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        org.assertj.core.api.Assertions.assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(vitalSurge);
    }
}
