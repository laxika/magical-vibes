package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.b.BishopOfTheBloodstained;
import com.github.laxika.magicalvibes.cards.r.RampagingFerocidon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GishathSunsAvatar.class, ColossalDreadmaw.class, AncientBrontodon.class,
        Forest.class, Opt.class, BishopOfTheBloodstained.class, RampagingFerocidon.class})
class GishathSunsAvatarTest extends BaseCardTest {


    @Test
    @DisplayName("Reveals cards equal to combat damage dealt and allows choosing Dinosaur creatures")
    void revealsCardsEqualToDamageAndAllowsChoosingDinosaurs() {
        Card dino1 = new ColossalDreadmaw(); // Dinosaur creature
        Card dino2 = new AncientBrontodon(); // Dinosaur creature
        Card forest = new Forest();          // Land — not eligible
        Card opt = new Opt();            // Instant — not eligible
        setupLibrary(List.of(dino1, dino2, forest, opt));

        harness.setLife(player2, 20);

        // Gishath is 7/6, deals 7 combat damage unblocked
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        // Player2 takes 7 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        // Should be awaiting library reveal choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose both Dinosaur creatures
        harness.handleMultipleCardsChosen(player1, List.of(dino1.getId(), dino2.getId()));

        // Both Dinosaurs should be on the battlefield
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Ancient Brontodon");

        // Remaining cards (Forest + Opt) should be on the bottom of the library
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(2);
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Opt");
    }

    @Test
    @DisplayName("Can choose zero Dinosaur creatures — all revealed cards go to bottom")
    void choosingNothingPutsAllOnBottom() {
        Card dino = new ColossalDreadmaw();
        Card forest = new Forest();
        Card opt = new Opt();
        setupLibrary(List.of(dino, forest, opt));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose nothing
        harness.handleMultipleCardsChosen(player1, List.of());

        // No new creatures on battlefield (only Gishath itself)
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");

        // All 3 cards should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }


    @Test
    @DisplayName("Non-Dinosaur creature cards are not eligible")
    void nonDinosaurCreatureNotEligible() {
        Card dino = new ColossalDreadmaw();
        Card bishop = new BishopOfTheBloodstained(); // non-Dinosaur creature
        setupLibrary(List.of(dino, bishop));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Bishop should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleCardsChosen(player1, List.of(bishop.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(dino.getId()));
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bishop);
    }


    @Test
    @DisplayName("When no Dinosaur creature cards are found, all go to bottom immediately")
    void noEligibleCardsAllToBottom() {
        Card forest = new Forest();
        Card opt = new Opt();
        Card bishop = new BishopOfTheBloodstained();
        setupLibrary(List.of(forest, opt, bishop));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        // No choice should be needed
        assertThat(gd.interaction.activeInteraction()).isNull();

        // All cards should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }


    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        setupLibrary(List.of());

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }


    @Test
    @DisplayName("Fewer cards in library than damage reveals all available")
    void fewerCardsThanDamage() {
        Card dino = new ColossalDreadmaw();
        setupLibrary(List.of(dino)); // only 1 card, but 7 damage

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(dino.getId()));

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }


    @Test
    @DisplayName("No trigger when Gishath is blocked and deals no player damage")
    void noTriggerWhenBlockedNoDamageToPlayer() {
        // Use a very large blocker so Gishath deals 0 to the player
        // Gishath has 7 power and trample, blocker needs >= 7 toughness to absorb all damage
        Card dino = new ColossalDreadmaw();
        setupLibrary(List.of(dino));

        harness.setLife(player2, 20);
        Permanent gishath = addCreatureReady(player1, new GishathSunsAvatar());
        gishath.setAttacking(true);

        Permanent blocker1 = addCreatureReady(player2, new AncientBrontodon());
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        // No player damage = no trigger = no library reveal choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        // Library should be untouched
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(blocker1.getMarkedDamage()).isEqualTo(7);
    }


    @Test
    @DisplayName("Can choose only some Dinosaur creatures, rest go to bottom")
    void partialSelectionPutsSomeOnBattlefieldRestOnBottom() {
        Card dino1 = new ColossalDreadmaw();
        Card dino2 = new AncientBrontodon();
        Card forest = new Forest();
        setupLibrary(List.of(dino1, dino2, forest));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        // Choose only dino1
        harness.handleMultipleCardsChosen(player1, List.of(dino1.getId()));

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertNotOnBattlefield(player1, "Ancient Brontodon");

        // dino2 and forest should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Ancient Brontodon", "Forest");
    }


    @Test
    @DisplayName("Reveals only the damage amount and puts the rest below unrevealed cards")
    void remainingCardsGoBelowUnrevealedLibrary() {
        Card dino = new AncientBrontodon();
        List<Card> revealed = new ArrayList<>();
        revealed.add(dino);
        for (int i = 0; i < 6; i++) {
            revealed.add(new Forest());
        }
        Card unrevealed1 = new Opt();
        Card unrevealed2 = new ColossalDreadmaw();
        List<Card> library = new ArrayList<>(revealed);
        library.add(unrevealed1);
        library.add(unrevealed2);
        setupLibrary(library);

        resolveCombatWithGishath();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed1, unrevealed2);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(unrevealed2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(dino.getId()));

        harness.assertOnBattlefield(player1, "Ancient Brontodon");
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining).hasSize(8);
        assertThat(remaining.subList(0, 2)).containsExactly(unrevealed1, unrevealed2);
        assertThat(remaining.subList(2, 8)).containsExactlyInAnyOrderElementsOf(revealed.subList(1, 7));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Trample reveals cards equal to damage to the player, not Gishath's power")
    void trampleUsesActualPlayerDamage() {
        Card revealed = new AncientBrontodon();
        Card unrevealed = new ColossalDreadmaw();
        setupLibrary(List.of(revealed, unrevealed));
        Permanent gishath = addCreatureReady(player1, new GishathSunsAvatar());
        gishath.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);
        harness.handleMultipleCardsChosen(player1, List.of(revealed.getId()));
        harness.assertOnBattlefield(player1, "Ancient Brontodon");
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Dinosaurs enter simultaneously so Ferocidon sees the other selected Dinosaur")
    void selectedDinosaursEnterSimultaneously() {
        Card dreadmaw = new ColossalDreadmaw();
        Card ferocidon = new RampagingFerocidon();
        setupLibrary(List.of(dreadmaw, ferocidon));
        harness.setLife(player1, 20);

        resolveCombatWithGishath();
        harness.handleMultipleCardsChosen(player1, List.of(dreadmaw.getId(), ferocidon.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Rampaging Ferocidon");
        harness.assertLife(player1, 19);
    }

    private void setupLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void resolveCombatWithGishath() {
        Permanent gishath = addCreatureReady(player1, new GishathSunsAvatar());
        gishath.setAttacking(true);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();
    }
}
