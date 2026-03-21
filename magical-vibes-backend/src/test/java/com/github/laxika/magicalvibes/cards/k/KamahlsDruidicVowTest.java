package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KamahlsDruidicVowTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effect configuration")
    void hasCorrectEffect() {
        KamahlsDruidicVow card = new KamahlsDruidicVow();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.class);
        LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect effect =
                (LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.alwaysEligiblePredicate()).isInstanceOf(CardTypePredicate.class);
        assertThat(effect.mvCappedEligiblePredicate()).isInstanceOf(CardAllOfPredicate.class);
    }

    // ===== Legendary sorcery casting restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // non-legendary
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed()); // legendary creature
        setupTopCards(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Kamahl's Druidic Vow");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can cast when controlling a legendary planeswalker")
    void canCastWithLegendaryPlaneswalker() {
        AjaniGoldmane ajani = new AjaniGoldmane();
        Permanent ajaniPerm = new Permanent(ajani);
        ajaniPerm.setLoyaltyCounters(4);
        ajaniPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ajaniPerm);

        setupTopCards(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kamahl's Druidic Vow");
    }

    // ===== Resolution: eligible card filtering =====

    @Test
    @DisplayName("Land cards are eligible regardless of mana value")
    void landCardsAreAlwaysEligible() {
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(forest, new GrizzlyBears(), new Shock()));

        castAndResolve(3);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose the forest
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Legendary creature with MV <= X is eligible")
    void legendaryCreatureWithMVLessOrEqualXIsEligible() {
        Card arvad = new ArvadTheCursed(); // MV 5
        setupTopCardsWithLegendary(List.of(arvad, new GrizzlyBears(), new Shock()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose Arvad
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(arvad.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Arvad the Cursed"));
    }

    @Test
    @DisplayName("Legendary creature with MV > X is NOT eligible")
    void legendaryCreatureWithMVGreaterThanXIsNotEligible() {
        Card arvad = new ArvadTheCursed(); // MV 5
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(arvad, forest, new GrizzlyBears()));

        castAndResolve(4); // X=4, Arvad MV=5 → not eligible

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Only forest should be selectable, not Arvad
        assertThatThrownBy(() ->
                harness.handleMultipleGraveyardCardsChosen(player1, List.of(arvad.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-legendary creature is NOT eligible")
    void nonLegendaryCreatureIsNotEligible() {
        Card bears = new GrizzlyBears(); // non-legendary, MV 2
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(bears, forest, new Shock()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Bears should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleGraveyardCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Instants and sorceries are NOT eligible")
    void nonPermanentCardsNotEligible() {
        Card shock = new Shock();
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(shock, forest, new GrizzlyBears()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        // Shock should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolution: choosing cards =====

    @Test
    @DisplayName("Choosing eligible cards puts them onto battlefield, rest to graveyard")
    void choosingPutsOnBattlefieldRestToGraveyard() {
        Card forest = new Forest();
        Card arvad = new ArvadTheCursed(); // MV 5
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        setupTopCardsWithLegendary(List.of(forest, arvad, bears, shock));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        // Choose both forest and arvad
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(forest.getId(), arvad.getId()));

        // Forest and Arvad should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Arvad the Cursed"));

        // Bears and Shock should be in graveyard (along with the Vow itself)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Choosing zero cards puts all into graveyard")
    void choosingNothingPutsAllInGraveyard() {
        Card forest = new Forest();
        Card arvad = new ArvadTheCursed();
        Card bears = new GrizzlyBears();
        setupTopCardsWithLegendary(List.of(forest, arvad, bears));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        // Choose nothing
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        // All three should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Arvad the Cursed"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Nothing extra on battlefield (only the legendary creature we used for casting)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== Resolution: no eligible cards =====

    @Test
    @DisplayName("When no eligible cards found, all go to graveyard immediately")
    void noEligibleCardsAllToGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        setupTopCardsWithLegendary(List.of(bears, shock));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        // No choice should be needed
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Both should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("X=0 looks at zero cards and does nothing")
    void xZeroDoesNothing() {
        setupTopCardsWithLegendary(List.of(new Forest(), new GrizzlyBears()));

        castAndResolve(0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // Library should be unchanged (still has the cards)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fewer cards in library than X looks at all available")
    void fewerCardsThanX() {
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(forest)); // only 1 card in library

        castAndResolve(5); // X=5 but only 1 card

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose the forest
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kamahl's Druidic Vow goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCardsWithLegendary(List.of(new GrizzlyBears(), new Shock()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kamahl's Druidic Vow"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    /**
     * Sets up library top cards and adds a legendary creature to the battlefield
     * so that the legendary sorcery can be cast.
     */
    private void setupTopCardsWithLegendary(List<Card> libraryCards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(libraryCards);
        harness.addToBattlefield(player1, new ArvadTheCursed());
    }

    /**
     * Sets up hand and mana, casts Kamahl's Druidic Vow with the given X value,
     * and resolves it.
     */
    private void castAndResolve(int xValue) {
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2); // {X}{G}{G}

        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
