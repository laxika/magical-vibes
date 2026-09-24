package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiarsPendulum.class, MyrRetriever.class, Bonesplitter.class})
class LiarsPendulumTest extends BaseCardTest {

    @Test
    void wrongGuessOffersRevealAndDraw() {
        addReadyPendulum();
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "No");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Myr Retriever");
        harness.assertInHand(player1, "Bonesplitter");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    void correctGuessDoesNotOfferRevealOrDraw() {
        addReadyPendulum();
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "Yes");

        harness.assertInHand(player1, "Myr Retriever");
        harness.assertNotInHand(player1, "Bonesplitter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    void wrongGuessWhenNamedCardIsAbsentOffersRevealAndDraw() {
        addReadyPendulum();
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "Yes");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Myr Retriever");
        harness.assertInHand(player1, "Bonesplitter");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    void correctGuessWhenNamedCardIsAbsentDoesNotOfferRevealOrDraw() {
        addReadyPendulum();
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "No");

        harness.assertInHand(player1, "Myr Retriever");
        harness.assertNotInHand(player1, "Bonesplitter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    void decliningRevealDoesNotDraw() {
        addReadyPendulum();
        harness.setHand(player1, List.of(new MyrRetriever()));
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "No");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Myr Retriever");
        harness.assertNotInHand(player1, "Bonesplitter");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    void cannotTargetItsController() {
        addReadyPendulum();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void activateAndChooseName(String cardName) {
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, cardName);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    private void addReadyPendulum() {
        addCreatureReady(player1, new LiarsPendulum());
    }
}
