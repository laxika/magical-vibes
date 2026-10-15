package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BasilicaScreecher;
import com.github.laxika.magicalvibes.cards.b.BorosCharm;
import com.github.laxika.magicalvibes.cards.o.OrzhovGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurgeTheProfane.class, BasilicaScreecher.class, BorosCharm.class, OrzhovGuildgate.class})
class PurgeTheProfaneTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards and the caster gains 2 life")
    void opponentDiscardsTwoAndCasterGainsLife() {
        harness.setHand(player2, List.of(new BasilicaScreecher(), new BorosCharm(), new OrzhovGuildgate()));
        harness.setLife(player1, 20);
        castPurgeTheProfane();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Purge the Profane");
    }

    @Test
    @DisplayName("Caster still gains 2 life when the opponent's hand is empty")
    void gainsLifeWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        castPurgeTheProfane();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PurgeTheProfane()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Opponent with one card discards it and the caster still gains 2 life")
    void discardsOnlyAvailableCardAndGainsLife() {
        harness.setHand(player2, List.of(new BorosCharm()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        castPurgeTheProfane();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Boros Charm");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Purge the Profane");
    }

    @Test
    @DisplayName("Opponent chooses the two discards before the caster gains life")
    void opponentChoosesDiscardsBeforeLifeGain() {
        harness.setHand(player2, List.of(new BasilicaScreecher(), new BorosCharm(), new OrzhovGuildgate()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        castPurgeTheProfane();

        harness.assertLife(player1, 20);
        harness.handleCardChosen(player2, 2);
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Orzhov Guildgate");
        harness.assertInGraveyard(player2, "Basilica Screecher");
        harness.assertInHand(player2, "Boros Charm");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPurgeTheProfane() {
        harness.setHand(player1, List.of(new PurgeTheProfane()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
