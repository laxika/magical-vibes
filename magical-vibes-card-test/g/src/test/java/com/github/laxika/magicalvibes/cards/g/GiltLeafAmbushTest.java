package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiltLeafAmbush.class, Forest.class})
class GiltLeafAmbushTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiltLeafAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {2}{G}
    }

    // Caster (player1) wins the clash: their revealed Ambush (MV 3) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GiltLeafAmbush(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GiltLeafAmbush(), new Forest(), new Forest()));
    }

    private void resolveKeepingRevealedCards() {
        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private List<Permanent> elfWarriors() {
        return findPermanents(player1, "Elf Warrior");
    }

    @Test
    @DisplayName("Always creates two 1/1 Elf Warrior tokens")
    void createsTwoTokens() {
        prepare();
        stackClashLossForCaster();

        resolveKeepingRevealedCards();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> {
            assertThat(t.getEffectivePower()).isEqualTo(1);
            assertThat(t.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Winning the clash grants deathtouch to both created tokens")
    void wonClashGrantsDeathtouch() {
        prepare();
        stackClashWinForCaster();

        resolveKeepingRevealedCards();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("Losing the clash leaves the tokens without deathtouch")
    void lostClashNoDeathtouch() {
        prepare();
        stackClashLossForCaster();

        resolveKeepingRevealedCards();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("Clash-win deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        prepare();
        stackClashWinForCaster();

        resolveKeepingRevealedCards();

        assertThat(elfWarriors()).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("Tied mana values create tokens without deathtouch")
    void tiedClashDoesNotGrantDeathtouch() {
        prepare();
        harness.setLibrary(player1, List.of(new GiltLeafAmbush(), new Forest()));
        harness.setLibrary(player2, List.of(new GiltLeafAmbush(), new Forest()));

        resolveKeepingRevealedCards();

        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("Bottoming the winning card does not change the clash result")
    void bottomingWinningCardStillGrantsDeathtouch() {
        prepare();
        GiltLeafAmbush revealed = new GiltLeafAmbush();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0);
        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("Winning grants deathtouch only to tokens from the current Ambush")
    void previousAmbushTokensDoNotGainDeathtouch() {
        prepare();
        stackClashLossForCaster();
        resolveKeepingRevealedCards();
        List<Permanent> previousTokens = List.copyOf(elfWarriors());

        prepare();
        stackClashWinForCaster();
        resolveKeepingRevealedCards();

        assertThat(elfWarriors()).hasSize(4);
        assertThat(previousTokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
        assertThat(elfWarriors().stream().filter(t -> !previousTokens.contains(t)).toList())
                .hasSize(2).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("A revealed land wins against an empty opposing library")
    void landWinsAgainstEmptyLibrary() {
        prepare();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of());

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("No player wins when both libraries are empty")
    void emptyLibrariesDoNotGrantDeathtouch() {
        prepare();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.castAndResolveInstant(player1, 0);

        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("The active opponent chooses clash placement before the caster")
    void opponentChoosesFirstOnTheirTurn() {
        prepare();
        harness.forceActivePlayer(player2);
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry choice = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(elfWarriors()).hasSize(2)
                .allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("Clash cards move only after both players choose their placement")
    void clashPlacementsAreSimultaneous() {
        prepare();
        GiltLeafAmbush revealed = new GiltLeafAmbush();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
    }
}
