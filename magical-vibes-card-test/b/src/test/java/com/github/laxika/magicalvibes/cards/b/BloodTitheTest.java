package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodTithe.class, LeylineOfSanctity.class})
class BloodTitheTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blood Tithe puts it on the stack")
    void castingPutsOnStack() {
        BloodTithe spell = new BloodTithe();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("Each opponent loses 3 life and controller gains life equal to life lost")
    void opponentLoses3LifeControllerGains3Life() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Blood Tithe goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blood Tithe");
    }

    @Test
    @DisplayName("Life loss can bring opponent below zero and controller still gains the full amount")
    void lifeLossCanBringBelowZero() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Opponent goes to -1 life (2 - 3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-1);
        // Controller gains 3 life (the full amount lost, not capped)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Opponent losing life to 0 or below ends the game via state-based actions")
    void lifeLossToZeroEndsGame() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // CR 704.5a — opponent at 0 life loses the game
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Blood Tithe affects an opponent with hexproof because it does not target")
    void affectsOpponentWithHexproof() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }
}
