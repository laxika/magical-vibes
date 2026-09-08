package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CombatMedic;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.IcatianJavelineers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HymnToTourach.class, CombatMedic.class, IcatianInfantry.class, IcatianJavelineers.class})
class HymnToTourachTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards at random")
    void targetPlayerDiscardsTwoCardsAtRandom() {
        harness.setHand(player2, List.of(new CombatMedic(), new IcatianInfantry(), new IcatianJavelineers()));
        harness.setHand(player1, List.of(new HymnToTourach()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gameLogContains("at random")).isTrue();
    }

    @Test
    @DisplayName("The controller may be targeted")
    void controllerMayBeTargeted() {
        harness.setHand(player1, List.of(
                new CombatMedic(), new IcatianInfantry(), new IcatianJavelineers(), new HymnToTourach()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 3, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Target player with fewer than two cards discards their whole hand")
    void targetPlayerWithOneCardDiscardsIt() {
        harness.setHand(player2, List.of(new CombatMedic()));
        harness.setHand(player1, List.of(new HymnToTourach()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target player with an empty hand discards nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HymnToTourach()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
