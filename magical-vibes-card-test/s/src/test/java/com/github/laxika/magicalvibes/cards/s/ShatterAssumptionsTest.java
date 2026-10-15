package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterAssumptions.class, BorosSwiftblade.class, DarksteelRelic.class,
        Forest.class, GrizzlyBears.class})
class ShatterAssumptionsTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless nonland mode discards only colorless nonland cards")
    void colorlessNonlandMode() {
        DarksteelRelic colorlessNonland = new DarksteelRelic();
        Forest land = new Forest();
        GrizzlyBears monocolored = new GrizzlyBears();
        BorosSwiftblade multicolored = new BorosSwiftblade();
        harness.setHand(player2, new ArrayList<>(List.of(
                colorlessNonland, land, monocolored, multicolored)));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(land, monocolored, multicolored);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(colorlessNonland);
    }

    @Test
    @DisplayName("Multicolored mode discards all multicolored cards")
    void multicoloredMode() {
        DarksteelRelic colorless = new DarksteelRelic();
        Forest land = new Forest();
        GrizzlyBears monocolored = new GrizzlyBears();
        BorosSwiftblade multicolored = new BorosSwiftblade();
        harness.setHand(player2, new ArrayList<>(List.of(
                colorless, land, monocolored, multicolored)));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(colorless, land, monocolored);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(multicolored);
    }

    @Test
    @DisplayName("Both modes can target only an opponent")
    void bothModesRejectTheCasterAsTarget() {
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void discardsEveryMatchingCardAndRevealsTheEntireHand(int mode) throws Exception {
        List<Card> matches = mode == 0
                ? List.of(new DarksteelRelic(), new DarksteelRelic())
                : List.of(new BorosSwiftblade(), new BorosSwiftblade());
        ShatterAssumptions retained = new ShatterAssumptions();
        harness.setHand(player2, List.of(matches.get(0), retained, matches.get(1)));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castAndResolveSorcery(player1, 0, mode, player2.getId());
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(matches);
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(matches.get(0).getId(), retained.getId(), matches.get(1).getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void handWithNoMatchesIsStillRevealed(int mode) throws Exception {
        ShatterAssumptions retained = new ShatterAssumptions();
        harness.setHand(player2, List.of(retained));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castAndResolveSorcery(player1, 0, mode, player2.getId());
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(retained.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void resolvesAgainstAnEmptyHand(int mode) {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, mode, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shatter Assumptions");
        assertThat(gd.stack).isEmpty();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
