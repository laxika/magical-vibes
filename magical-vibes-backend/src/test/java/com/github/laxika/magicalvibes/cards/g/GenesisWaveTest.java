package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.CarnifexDemon;
import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GenesisWaveEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenesisWaveTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has GenesisWaveEffect on SPELL slot")
    void hasCorrectStructure() {
        GenesisWave card = new GenesisWave();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(GenesisWaveEffect.class);
    }

    // ===== X=0 reveals nothing =====

    @Test
    @DisplayName("Casting with X=0 reveals no cards")
    void xZeroRevealsNothing() {
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should not be awaiting any input
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Puts eligible permanents onto battlefield =====

    @Test
    @DisplayName("Eligible permanent cards can be put onto the battlefield")
    void putsEligiblePermanentsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card spellbomb = new FlightSpellbomb();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest, spellbomb));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 6); // {3}{G}{G}{G} with X=3

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        // Should be awaiting library reveal choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Select all three cards (creature MV 2, land MV 0, artifact MV 1 — all <= 3)
        harness.handleMultipleGraveyardCardsChosen(player1,
                List.of(bears.getId(), forest.getId(), spellbomb.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Flight Spellbomb");
    }

    // ===== Non-permanents go to graveyard =====

    @Test
    @DisplayName("Instants and sorceries revealed go to graveyard")
    void nonPermanentsGoToGraveyard() {
        Card bears = new GrizzlyBears();
        Card blaze = new Blaze();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, blaze));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Only bears should be selectable; select it
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Blaze (sorcery) should be in graveyard
        harness.assertInGraveyard(player1, "Blaze");
    }

    // ===== Permanents with MV > X go to graveyard =====

    @Test
    @DisplayName("Permanent cards with mana value greater than X go to graveyard")
    void permanentsWithHighMVGoToGraveyard() {
        Card forest = new Forest();          // MV 0 — eligible (0 <= 3)
        Card bears = new GrizzlyBears();     // MV 2 — eligible (2 <= 3)
        Card demon = new CarnifexDemon();    // MV 6 — NOT eligible (6 > 3)

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears, demon));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 6); // X=3

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        // Forest and Bears are eligible, Demon (MV 6) is not
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Carnifex Demon");
    }

    // ===== Player can choose subset =====

    @Test
    @DisplayName("Player can choose to put only some eligible cards onto the battlefield")
    void playerCanChooseSubset() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Only select bears, not forest
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Forest was not selected — goes to graveyard
        harness.assertInGraveyard(player1, "Forest");
    }

    // ===== Player can choose zero cards =====

    @Test
    @DisplayName("Player can choose zero cards — all revealed go to graveyard")
    void playerCanChooseZeroCards() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Select nothing
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    // ===== No eligible cards — all go straight to graveyard =====

    @Test
    @DisplayName("When no revealed cards are eligible, all go to graveyard without prompting")
    void noEligibleCardsAllToGraveyard() {
        Card blaze1 = new Blaze();
        Card blaze2 = new Blaze();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(blaze1, blaze2));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // No interaction expected — both sorceries go to graveyard
        assertThat(gd.interaction.awaitingInputType()).isNull();
        harness.assertInGraveyard(player1, "Blaze");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Blaze")).count()).isEqualTo(2);
    }

    // ===== Creatures get ETB processing =====

    @Test
    @DisplayName("Creatures put onto battlefield get ETB effects processed")
    void creaturesGetETBProcessing() {
        Card demon = new CarnifexDemon(); // ETB: enters with two -1/-1 counters

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(demon);

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 9); // X=6

        harness.castSorcery(player1, 0, 6);
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(demon.getId()));

        // Resolve ETB triggered ability (PutCountersOnSourceEffect)
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Carnifex Demon");

        // Carnifex Demon should have two -1/-1 counters from its ETB
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carnifex Demon"))
                .findFirst().orElseThrow()
                .getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== Library smaller than X =====

    @Test
    @DisplayName("When library has fewer than X cards, reveals all available cards")
    void librarySmallerThanX() {
        Card bears = new GrizzlyBears();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(bears); // Only 1 card, but X=5

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 8); // X=5

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        // Should reveal the 1 card and prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
