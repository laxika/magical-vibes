package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Miscalculation.class, GiantCockroach.class})
class MiscalculationTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent cannot pay {2}")
    void countersWhenOpponentCannotPay() {
        GiantCockroach cockroach = new GiantCockroach();
        harness.castFromHand(player1, cockroach, "{3}{B}");

        harness.setHand(player2, List.of(new Miscalculation()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, cockroach.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Cockroach");
        harness.assertNotOnBattlefield(player1, "Giant Cockroach");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {2}")
    void spellNotCounteredWhenOpponentPays() {
        GiantCockroach cockroach = new GiantCockroach();
        harness.castFromHand(player1, cockroach, "{3}{B}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Miscalculation()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, cockroach.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        GiantCockroach cockroach = new GiantCockroach();
        harness.castFromHand(player1, cockroach, "{3}{B}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Miscalculation()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, cockroach.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Giant Cockroach");
        harness.assertNotOnBattlefield(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Miscalculation()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Miscalculation");
        harness.assertInHand(player1, "Giant Cockroach");
    }
}
