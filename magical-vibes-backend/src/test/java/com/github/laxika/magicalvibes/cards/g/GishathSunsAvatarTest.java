package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GishathSunsAvatarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER effect with Dinosaur creature predicate")
    void hasCombatDamageToPlayerEffect() {
        GishathSunsAvatar card = new GishathSunsAvatar();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER))
                .hasSize(1)
                .first()
                .isInstanceOf(LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.class);

        LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect effect =
                (LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect)
                        card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(effect.remainingToBottomRandom()).isTrue();
        assertThat(effect.alwaysEligiblePredicate()).isNotNull();
        assertThat(effect.mvCappedEligiblePredicate()).isNull();
    }

    // ===== Combat damage trigger: reveals correct number of cards =====

    @Test
    @DisplayName("Reveals cards equal to combat damage dealt and allows choosing Dinosaur creatures")
    void revealsCardsEqualToDamageAndAllowsChoosingDinosaurs() {
        Card dino1 = new ColossalDreadmaw(); // Dinosaur creature
        Card dino2 = new AncientBrontodon(); // Dinosaur creature
        Card forest = new Forest();          // Land — not eligible
        Card shock = new Shock();            // Instant — not eligible
        setupLibrary(List.of(dino1, dino2, forest, shock));

        harness.setLife(player2, 20);

        // Gishath is 7/6, deals 7 combat damage unblocked
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        // Player2 takes 7 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        // Should be awaiting library reveal choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose both Dinosaur creatures
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(dino1.getId(), dino2.getId()));

        // Both Dinosaurs should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Colossal Dreadmaw"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ancient Brontodon"));

        // Remaining cards (Forest + Shock) should be on the bottom of the library
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(2);
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock");
    }

    @Test
    @DisplayName("Can choose zero Dinosaur creatures — all revealed cards go to bottom")
    void choosingNothingPutsAllOnBottom() {
        Card dino = new ColossalDreadmaw();
        Card forest = new Forest();
        Card shock = new Shock();
        setupLibrary(List.of(dino, forest, shock));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose nothing
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // No new creatures on battlefield (only Gishath itself)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Colossal Dreadmaw"));

        // All 3 cards should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    // ===== Non-Dinosaur creatures are not eligible =====

    @Test
    @DisplayName("Non-Dinosaur creature cards are not eligible")
    void nonDinosaurCreatureNotEligible() {
        Card dino = new ColossalDreadmaw();
        Card bears = new GrizzlyBears(); // non-Dinosaur creature
        setupLibrary(List.of(dino, bears));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Bears should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No eligible cards =====

    @Test
    @DisplayName("When no Dinosaur creature cards are found, all go to bottom immediately")
    void noEligibleCardsAllToBottom() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        setupLibrary(List.of(forest, shock, bears));

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        // No choice should be needed
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // All cards should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        setupLibrary(List.of());

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Fewer cards than damage =====

    @Test
    @DisplayName("Fewer cards in library than damage reveals all available")
    void fewerCardsThanDamage() {
        Card dino = new ColossalDreadmaw();
        setupLibrary(List.of(dino)); // only 1 card, but 7 damage

        harness.setLife(player2, 20);
        resolveCombatWithGishath();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(dino.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Colossal Dreadmaw"));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== No trigger when blocked (no player damage) =====

    @Test
    @DisplayName("No trigger when Gishath is blocked and deals no player damage")
    void noTriggerWhenBlockedNoDamageToPlayer() {
        // Use a very large blocker so Gishath deals 0 to the player
        // Gishath has 7 power and trample, blocker needs >= 7 toughness to absorb all damage
        Card dino = new ColossalDreadmaw();
        setupLibrary(List.of(dino));

        harness.setLife(player2, 20);
        Permanent gishath = addGishathReady(player1);
        gishath.setAttacking(true);

        // Add a large blocker (we'll simulate with a 7+ toughness creature)
        // Gishath has trample, so all damage is assigned to blocker up to its toughness.
        // We need a blocker with toughness >= 7 to absorb all 7 damage.
        // Use two blockers to absorb the damage
        Permanent blocker1 = addCreatureReady(player2, new ColossalDreadmaw()); // 6/6
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        Permanent blocker2 = addCreatureReady(player2, new ColossalDreadmaw()); // 6/6
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No player damage = no trigger = no library reveal choice
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
        // Library should be untouched
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    // ===== Partial selection =====

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
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(dino1.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Colossal Dreadmaw"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ancient Brontodon"));

        // dino2 and forest should be on the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Ancient Brontodon", "Forest");
    }

    // ===== Helpers =====

    private void setupLibrary(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private Permanent addGishathReady(Player player) {
        Permanent perm = new Permanent(new GishathSunsAvatar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombatWithGishath() {
        Permanent gishath = addGishathReady(player1);
        gishath.setAttacking(true);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
