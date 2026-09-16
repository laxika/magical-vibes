package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenewedFaith.class, GlorySeeker.class})
class RenewedFaithTest extends BaseCardTest {

    // ===== Main spell =====

    @Test
    @DisplayName("Casting Renewed Faith gains 6 life")
    void gainsSixLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new RenewedFaith(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    // ===== Cycling =====

    @Test
    @DisplayName("Cycling discards the card, draws one, and may gain 2 life when accepted")
    void cyclingAcceptGainsTwoLifeAndDraws() {
        harness.setHand(player1, List.of(new RenewedFaith()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(player1, "Renewed Faith");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Declining the cycle trigger still draws but gains no life")
    void cyclingDeclineDrawsWithoutLife() {
        harness.setHand(player1, List.of(new RenewedFaith()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Renewed Faith");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
