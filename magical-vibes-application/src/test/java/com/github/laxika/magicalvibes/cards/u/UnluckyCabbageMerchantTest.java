package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

    private Permanent castMerchant() {
        UnluckyCabbageMerchant merchantCard = new UnluckyCabbageMerchant();
        harness.setHand(player1, List.of(merchantCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == merchantCard)
                .findFirst()
                .orElseThrow();
    }
}
