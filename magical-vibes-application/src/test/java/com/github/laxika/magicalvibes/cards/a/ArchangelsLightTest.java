package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchangelsLightTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Archangel's Light does not target")
    void doesNotTarget() {
        ArchangelsLight card = new ArchangelsLight();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainLifePerGraveyardCardEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleGraveyardIntoLibraryEffect.class);
    }

    // ===== Life gain =====

    @Test
    @DisplayName("Gains 2 life for each card in graveyard")
    void gains2LifePerGraveyardCard() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bear1, bear2, spider));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 3 cards in graveyard × 2 life = 6 life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Gains no life when graveyard is empty")
    void gainsNoLifeWithEmptyGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Graveyard shuffle =====

    @Test
    @DisplayName("Shuffles graveyard into library after gaining life")
    void shufflesGraveyardIntoLibrary() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Graveyard should only contain Archangel's Light itself (sorcery goes to graveyard after resolution)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Library should have 2 more cards (the bears)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
    }

    // ===== Combined behavior =====

    @Test
    @DisplayName("Life gain counts cards before shuffle")
    void lifeGainCountsCardsBeforeShuffle() {
        // 5 cards in graveyard
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GiantSpider(), new GiantSpider()));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 5 cards × 2 life = 10 life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 10);
        // Graveyard shuffled into library (only Archangel's Light remains in graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Giant Spider"));
    }

    // ===== Stack / resolution =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Archangel's Light goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Archangel's Light"));
    }
}
