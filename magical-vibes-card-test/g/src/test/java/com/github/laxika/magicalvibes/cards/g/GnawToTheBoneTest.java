package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mulch;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnawToTheBone.class, WalkingCorpse.class, Mulch.class})
class GnawToTheBoneTest extends BaseCardTest {

    

    @Test
    @DisplayName("Gains 2 life per creature card in graveyard")
    void gains2LifePerCreatureInGraveyard() {
        // Put 3 creature cards in the graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new WalkingCorpse());
        graveyard.add(new WalkingCorpse());
        graveyard.add(new WalkingCorpse());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // 3 creatures * 2 life = 6 life gained, 20 + 6 = 26
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        // 2 creatures + 1 non-creature in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new WalkingCorpse());
        graveyard.add(new WalkingCorpse());
        graveyard.add(new Mulch());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // 2 creatures * 2 life = 4 life gained, 20 + 4 = 24
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains no life when no creature cards in graveyard")
    void gainsNoLifeWhenNoCreatures() {
        // Only non-creature cards in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new Mulch());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains no life when graveyard is empty")
    void gainsNoLifeWhenGraveyardEmpty() {
        harness.setGraveyard(player1, new ArrayList<>());

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flashback from graveyard gains life based on creature cards")
    void flashbackGainsLife() {
        // Put 2 creature cards + the Gnaw to the Bone itself in graveyard
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GnawToTheBone());
        graveyard.add(new WalkingCorpse());
        graveyard.add(new WalkingCorpse());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        // 2 creatures * 2 life = 4 life gained, 20 + 4 = 24
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GnawToTheBone());
        graveyard.add(new WalkingCorpse());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Gnaw to the Bone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gnaw to the Bone"));
    }

    @Test
    @DisplayName("Gnaw to the Bone goes to graveyard after normal cast resolves")
    void goesToGraveyardAfterResolving() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new WalkingCorpse());
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Gnaw to the Bone");
    }

    @Test
    @DisplayName("Only the caster's graveyard contributes to life gain")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts creatures added to the graveyard before resolution")
    void countsCreaturesAddedBeforeResolution() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not count creatures removed from the graveyard before resolution")
    void ignoresCreaturesRemovedBeforeResolution() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new GnawToTheBone()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of(new Mulch()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Gnaw to the Bone");
    }

    @Test
    @DisplayName("Flashback with no creatures gains no life and still exiles the spell")
    void flashbackWithNoCreaturesStillExiles() {
        GnawToTheBone spell = new GnawToTheBone();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player1, "Gnaw to the Bone");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
