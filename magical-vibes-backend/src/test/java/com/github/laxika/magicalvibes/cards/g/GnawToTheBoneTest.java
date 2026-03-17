package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnawToTheBoneTest extends BaseCardTest {

    @Test
    @DisplayName("Gnaw to the Bone has correct card properties")
    void hasCorrectProperties() {
        GnawToTheBone card = new GnawToTheBone();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(GainLifePerCreatureCardInGraveyardEffect.class);
        GainLifePerCreatureCardInGraveyardEffect effect =
                (GainLifePerCreatureCardInGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.lifePerCreature()).isEqualTo(2);
        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{2}{G}");
    }

    @Test
    @DisplayName("Gains 2 life per creature card in graveyard")
    void gains2LifePerCreatureInGraveyard() {
        // Put 3 creature cards in the graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 3 creatures * 2 life = 6 life gained, 20 + 6 = 26
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        // 2 creatures + 1 non-creature in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GiantGrowth());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2 creatures * 2 life = 4 life gained, 20 + 4 = 24
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains no life when no creature cards in graveyard")
    void gainsNoLifeWhenNoCreatures() {
        // Only non-creature cards in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GiantGrowth());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains no life when graveyard is empty")
    void gainsNoLifeWhenGraveyardEmpty() {
        harness.setGraveyard(player1, new ArrayList<>());

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flashback from graveyard gains life based on creature cards")
    void flashbackGainsLife() {
        // Put 2 creature cards + the Gnaw to the Bone itself in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GnawToTheBone());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2 creatures * 2 life = 4 life gained, 20 + 4 = 24
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GnawToTheBone());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gnaw to the Bone"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gnaw to the Bone"));
    }

    @Test
    @DisplayName("Gnaw to the Bone goes to graveyard after normal cast resolves")
    void goesToGraveyardAfterResolving() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gnaw to the Bone"));
    }
}
