package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Fasting;
import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NamelessRace.class, Squire.class, Fasting.class, GoblinHero.class})
class NamelessRaceTest extends BaseCardTest {

    @Test
    @DisplayName("Caps the life payment at white opposing permanents and graveyard cards")
    void capsLifePaymentAtWhiteOpposingObjects() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Squire());
        harness.setGraveyard(player2, List.of(new Squire()));

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
    @DisplayName("Counts only opposing white permanents and white graveyard cards")
    void excludesOwnAndNonwhiteObjects() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Squire());
        harness.addToBattlefield(player2, new Fasting());
        harness.addToBattlefield(player2, new GoblinHero());
        harness.setGraveyard(player2, List.of(new Squire(), new GoblinHero()));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1", "2");
    }

    @Test
    @DisplayName("Cannot pay more life than the dynamic cap or life total")
    void appliesBothLimits() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player2, new Squire());
        harness.setGraveyard(player2, List.of(new Squire(), new Squire()));

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

    @Test
    @DisplayName("Paying zero life leaves a 0/0 that dies to state-based actions")
    void payingZeroLifeDies() {
        harness.setLife(player1, 20);

        cast();
        harness.handleListChoice(player1, "0");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nameless Race"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nameless Race"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void cast() {
        harness.castFromHand(player1, new NamelessRace(), "{3}{B}");
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
