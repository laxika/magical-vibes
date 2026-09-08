package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(3, ManaColor.COLORLESS, ManaColor.WHITE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunscape Battlemage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Green kicker destroys a target creature with flying")
    void greenKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(4, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Air Elemental")).isZero();
    }

    @Test
    @DisplayName("Blue kicker draws two cards")
    void blueKicker() {
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(5, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Both kicker costs resolve their independent abilities")
    void bothKickers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(6, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"), target.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Air Elemental")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, false);
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
