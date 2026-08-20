package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NamelessRace.class, SavannahLions.class})
class NamelessRaceTest extends BaseCardTest {

    @Test
    @DisplayName("Caps the life payment at white opposing permanents and graveyard cards")
    void capsLifePaymentAtWhiteOpposingObjects() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setGraveyard(player2, List.of(new SavannahLions()));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1", "2");

        harness.handleListChoice(player1, "2");

        Permanent race = findPermanent(player1, "Nameless Race");
        assertThat(gqs.getEffectivePower(gd, race)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, race)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts only white nontoken permanents controlled by opponents")
    void excludesWhiteTokenPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, whiteToken());

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0");
    }

    @Test
    @DisplayName("Cannot pay more life than the dynamic cap or life total")
    void appliesBothLimits() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setGraveyard(player2, List.of(new SavannahLions(), new SavannahLions()));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1");
    }

    @Test
    @DisplayName("Rejects a life payment above the cap")
    void rejectsPaymentAboveCap() {
        cast();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void cast() {
        harness.setHand(player1, List.of(new NamelessRace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private static Card whiteToken() {
        Card token = new Card();
        token.setName("White Token");
        token.setColor(CardColor.WHITE);
        token.setColors(List.of(CardColor.WHITE));
        token.setToken(true);
        return token;
    }
}
