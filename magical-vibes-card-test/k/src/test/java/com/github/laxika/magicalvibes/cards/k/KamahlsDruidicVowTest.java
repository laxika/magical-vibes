package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({KamahlsDruidicVow.class, AjaniGoldmane.class, ArvadTheCursed.class,
        Forest.class, GrizzlyBears.class, Shock.class, MoxAmber.class, TatyovaBenthicDruid.class})
class KamahlsDruidicVowTest extends BaseCardTest {

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
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can cast when controlling a legendary planeswalker")
    void canCastWithLegendaryPlaneswalker() {
        Permanent ajaniPerm = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajaniPerm.setCounterCount(CounterType.LOYALTY, 4);
        ajaniPerm.setSummoningSick(false);

        setupTopCards(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Resolution: eligible card filtering =====

    @Test
    @DisplayName("Land cards are eligible regardless of mana value")
    void landCardsAreAlwaysEligible() {
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(forest, new GrizzlyBears(), new Shock()));

        castAndResolve(3);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose the forest
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Legendary creature with MV <= X is eligible")
    void legendaryCreatureWithMVLessOrEqualXIsEligible() {
        Card arvad = new ArvadTheCursed(); // MV 5
        setupTopCardsWithLegendary(List.of(arvad, new GrizzlyBears(), new Shock()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose Arvad
        harness.handleMultipleCardsChosen(player1, List.of(arvad.getId()));

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
    }

    @Test
    @DisplayName("Legendary creature with MV > X is NOT eligible")
    void legendaryCreatureWithMVGreaterThanXIsNotEligible() {
        Card arvad = new ArvadTheCursed(); // MV 5
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(arvad, forest, new GrizzlyBears()));

        castAndResolve(4); // X=4, Arvad MV=5 → not eligible

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Only forest should be selectable, not Arvad
        assertThatThrownBy(() ->
                harness.handleMultipleCardsChosen(player1, List.of(arvad.getId())))
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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Bears should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Instants and sorceries are NOT eligible")
    void nonPermanentCardsNotEligible() {
        Card shock = new Shock();
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(shock, forest, new GrizzlyBears()));

        castAndResolve(5);

        // Shock should not be selectable
        assertThatThrownBy(() ->
                harness.handleMultipleCardsChosen(player1, List.of(shock.getId())))
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

        // Choose both forest and arvad
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), arvad.getId()));

        // Forest and Arvad should be on battlefield
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Arvad the Cursed");

        // Bears and Shock should be in graveyard (along with the Vow itself)
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Choosing zero cards puts all into graveyard")
    void choosingNothingPutsAllInGraveyard() {
        Card forest = new Forest();
        Card arvad = new ArvadTheCursed();
        Card bears = new GrizzlyBears();
        setupTopCardsWithLegendary(List.of(forest, arvad, bears));

        castAndResolve(5);

        // Choose nothing
        harness.handleMultipleCardsChosen(player1, List.of());

        // All three should be in graveyard
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Nothing extra on battlefield (only the legendary creature we used for casting)
        harness.assertNotOnBattlefield(player1, "Forest");
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
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Both should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("X=0 looks at zero cards and does nothing")
    void xZeroDoesNothing() {
        setupTopCardsWithLegendary(List.of(new Forest(), new GrizzlyBears()));

        castAndResolve(0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Library should be unchanged (still has the cards)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fewer cards in library than X looks at all available")
    void fewerCardsThanX() {
        Card forest = new Forest();
        setupTopCardsWithLegendary(List.of(forest)); // only 1 card in library

        castAndResolve(5); // X=5 but only 1 card

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose the forest
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kamahl's Druidic Vow goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCardsWithLegendary(List.of(new GrizzlyBears(), new Shock()));

        castAndResolve(5);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Kamahl's Druidic Vow");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    @Test
    @DisplayName("A legendary planeswalker enters with its starting loyalty")
    void legendaryPlaneswalkerEntersWithLoyalty() {
        Card ajani = new AjaniGoldmane();
        setupTopCardsWithLegendary(List.of(ajani));

        castAndResolve(4);
        harness.handleMultipleCardsChosen(player1, List.of(ajani.getId()));

        Permanent entered = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == ajani)
                .findFirst().orElseThrow();
        assertThat(entered.getCounters()).containsEntry(CounterType.LOYALTY, 4);
    }

    @Test
    @DisplayName("A legendary artifact does not satisfy the casting restriction")
    void legendaryArtifactDoesNotPermitCasting() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's legendary creature does not permit casting")
    void opponentsLegendaryCreatureDoesNotPermitCasting() {
        harness.addToBattlefield(player2, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KamahlsDruidicVow()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A legendary artifact is eligible but a legendary sorcery is not")
    void legendaryArtifactEligibleAndLegendarySorceryExcluded() {
        Card mox = new MoxAmber();
        Card vow = new KamahlsDruidicVow();
        Card untouched = new Forest();
        setupTopCardsWithLegendary(List.of(mox, vow, untouched));

        castAndResolve(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(vow.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(mox.getId()));

        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).contains(vow);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    @DisplayName("A land put onto the battlefield triggers an existing Tatyova")
    void selectedLandTriggersLandfall() {
        Card forest = new Forest();
        Card draw = new Forest();
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(forest, draw));

        castAndResolve(1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(draw);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Tatyova sees a land entering simultaneously even when the land is first in the library")
    void simultaneouslyEnteringTatyovaSeesLand() {
        Card forest = new Forest();
        Card tatyova = new TatyovaBenthicDruid();
        Card draw = new Forest();
        setupTopCardsWithLegendary(List.of(forest, tatyova, new KamahlsDruidicVow(),
                new KamahlsDruidicVow(), new KamahlsDruidicVow(), draw));
        harness.setLife(player1, 20);

        castAndResolve(5);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), tatyova.getId()));
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(draw);
        harness.assertOnBattlefield(player1, "Tatyova, Benthic Druid");
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    /**
     * Sets up library top cards and adds a legendary creature to the battlefield
     * so that the legendary sorcery can be cast.
     */
    private void setupTopCardsWithLegendary(List<Card> libraryCards) {
        harness.setLibrary(player1, libraryCards);
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
