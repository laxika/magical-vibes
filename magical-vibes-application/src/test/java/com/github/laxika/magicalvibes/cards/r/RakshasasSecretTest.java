package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakshasasSecret.class, AlpineGrizzly.class})
class RakshasasSecretTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards and the caster mills two cards")
    void opponentDiscardsTwoAndCasterMillsTwo() {
        AlpineGrizzly first = new AlpineGrizzly();
        AlpineGrizzly second = new AlpineGrizzly();
        AlpineGrizzly retained = new AlpineGrizzly();
        harness.setHand(player2, List.of(first, second, retained));
        int casterDeckSize = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(casterDeckSize);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(casterDeckSize - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        harness.assertInGraveyard(player1, "Rakshasa's Secret");
    }

    @Test
    @DisplayName("An empty opponent hand still results in the caster milling two cards")
    void emptyHandStillMillsTwo() {
        harness.setHand(player2, List.of());
        int casterDeckSize = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(casterDeckSize - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
    }

    @Test
    @DisplayName("An opponent with one card discards it and the caster still mills two")
    void oneCardHandStillMillsTwo() {
        AlpineGrizzly discarded = new AlpineGrizzly();
        harness.setHand(player2, List.of(discarded));
        int casterDeckSize = gd.playerDecks.get(player1.getId()).size();
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(casterDeckSize - 2);
    }

    @Test
    @DisplayName("Milling a one-card library puts its only card into the caster's graveyard")
    void millsOnlyAvailableCard() {
        AlpineGrizzly milled = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player2, List.of());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        harness.assertInGraveyard(player1, "Rakshasa's Secret");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new RakshasasSecret()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
