package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnluckyCabbageMerchant.class, Forest.class})
class UnluckyCabbageMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        castMerchant();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("May search for a tapped basic land and put itself on the bottom of its owner's library")
    void searchesForLandAndPutsItselfOnBottom() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent merchant = castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(merchant);
        assertThat(gd.playerDecks.get(player1.getId())).contains(merchant.getCard());
        assertThat(findPermanents(player1, "Forest")).anyMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Declining the search leaves the merchant on the battlefield")
    void decliningSearchLeavesMerchantOnBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent merchant = castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Searching an empty library still returns the merchant")
    void searchingEmptyLibraryReturnsMerchant() {
        harness.setLibrary(player1, List.of());
        Permanent merchant = castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(merchant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(merchant.getCard());
    }

    @Test
    @DisplayName("Failing to find a basic land still returns the merchant")
    void failingToFindReturnsMerchant() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent merchant = castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(merchant);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, merchant.getCard());
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Searching a library with no basic lands still returns the merchant")
    void searchingWithoutBasicLandsReturnsMerchant() {
        UnluckyCabbageMerchant libraryCard = new UnluckyCabbageMerchant();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent merchant = castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(merchant);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, merchant.getCard());
    }

    @Test
    @DisplayName("The Food gains three life even when the merchant's search is declined")
    void foodGainsLifeAfterDecliningSearch() {
        harness.setLife(player1, 10);
        castMerchant();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player1, "Unlucky Cabbage Merchant");
    }

    private Permanent castMerchant() {
        UnluckyCabbageMerchant merchantCard = new UnluckyCabbageMerchant();
        harness.castFromHand(player1, merchantCard, "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == merchantCard)
                .findFirst()
                .orElseThrow();
    }
}
