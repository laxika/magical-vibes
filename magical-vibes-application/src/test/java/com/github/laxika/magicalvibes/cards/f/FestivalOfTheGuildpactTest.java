package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FestivalOfTheGuildpact.class, Char.class})
class FestivalOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents X damage to its controller and draws a card")
    void preventsDamageAndDrawsCard() {
        Card drawnCard = new Char();
        harness.setHand(player1, List.of(new FestivalOfTheGuildpact()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Draws a card and prevents no damage when X is zero")
    void drawsCardAndPreventsNoDamageWhenXIsZero() {
        Card drawnCard = new Char();
        harness.setHand(player1, List.of(new FestivalOfTheGuildpact()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Consumes the shield across successive damage events and lets excess damage through")
    void shieldIsConsumedAcrossDamageEvents() {
        harness.setHand(player1, List.of(new FestivalOfTheGuildpact()));
        harness.setLibrary(player1, List.of(new Char()));
        harness.setHand(player2, List.of(new Char(), new Char(), new Char()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantForX(player1, 0, 5, List.of());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Unused prevention expires when the turn ends")
    void unusedShieldExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new FestivalOfTheGuildpact()));
        harness.setLibrary(player1, List.of(new Char(), new Char()));
        harness.setLibrary(player2, List.of(new Char(), new Char()));
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 4, List.of());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 18);
    }
}
