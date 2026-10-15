package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornvaultForager.class, TreeguardDuo.class})
class ThornvaultForagerTest extends BaseCardTest {

    @Test
    void firstAbilityAddsGreenMana() {
        Permanent forager = addReadyForager();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(forager.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityForagesByExilingThreeCardsAndAddsTwoChosenMana() {
        addReadyForager();
        harness.setGraveyard(player1, List.of(new TreeguardDuo(), new TreeguardDuo(), new TreeguardDuo()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void secondAbilityCanForageBySacrificingFood() {
        addReadyForager();
        Permanent food = addFoodToken();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void thirdAbilitySearchesForASquirrelCard() {
        addReadyForager();
        harness.setLibrary(player1, List.of(new ThornvaultForager(), new TreeguardDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Thornvault Forager");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Treeguard Duo");
    }

    @Test
    void forageManaAbilityDoesNotUseTheStack() {
        addReadyForager();
        harness.setGraveyard(player1, List.of(new TreeguardDuo(), new TreeguardDuo(), new TreeguardDuo()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateForageManaAbilityWithoutPayingForage() {
        Permanent forager = addReadyForager();
        harness.setGraveyard(player1, List.of(new TreeguardDuo(), new TreeguardDuo()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(forager.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentsFoodCannotPayForageCost() {
        Permanent forager = addReadyForager();
        Permanent food = addFoodToken();
        gd.playerBattlefields.get(player1.getId()).remove(food);
        gd.playerBattlefields.get(player2.getId()).add(food);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(forager.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(food);
    }

    @Test
    void summoningSicknessPreventsAllThreeTapAbilities() {
        harness.addToBattlefield(player1, new ThornvaultForager());
        harness.setGraveyard(player1, List.of(new TreeguardDuo(), new TreeguardDuo(), new TreeguardDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void searchCanFailToFindEvenWhenASquirrelIsPresent() {
        addReadyForager();
        harness.setLibrary(player1, List.of(new ThornvaultForager(), new TreeguardDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Thornvault Forager");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Thornvault Forager", "Treeguard Duo");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchWithoutSquirrelsLeavesLibraryCardsInLibrary() {
        Permanent forager = addReadyForager();
        harness.setLibrary(player1, List.of(new TreeguardDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(forager.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Treeguard Duo");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Treeguard Duo");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyForager() {
        return addCreatureReady(player1, new ThornvaultForager());
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        return harness.addToBattlefieldAndReturn(player1, food);
    }
}
