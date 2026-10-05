package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicDrain.class, Forest.class})
class PsychicDrainTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills X cards and the controller gains X life")
    void millsAndGainsLifeForX() {
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Can target the controller")
    void canTargetController() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 1, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Mills only available cards but still gains all X life")
    void millsOnlyAvailableCards() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        var permanent = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero leaves the target library and both life totals unchanged")
    void zeroXDoesNothing() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int targetLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, controllerLifeBefore);
        harness.assertLife(player2, targetLifeBefore);
        harness.assertInGraveyard(player1, "Psychic Drain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty target library does not prevent gaining X life")
    void gainsLifeWithEmptyTargetLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new PsychicDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int targetLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, controllerLifeBefore + 3);
        harness.assertLife(player2, targetLifeBefore);
        harness.assertInGraveyard(player1, "Psychic Drain");
        assertThat(gd.stack).isEmpty();
    }
}
