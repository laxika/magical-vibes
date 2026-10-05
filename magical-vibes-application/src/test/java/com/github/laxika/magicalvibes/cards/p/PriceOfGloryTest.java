package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CabalPit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriceOfGlory.class, CabalPit.class, Forest.class})
class PriceOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a land tapped for mana outside its controller's turn")
    void destroysLandTappedOutsideItsControllersTurn() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a land with an activated mana ability tapped outside its controller's turn")
    void destroysLandWithActivatedManaAbilityTappedOutsideItsControllersTurn() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player2, new CabalPit());

        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Cabal Pit");
        harness.assertInGraveyard(player2, "Cabal Pit");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not destroy a land tapped during its controller's turn")
    void doesNotDestroyLandTappedDuringItsControllersTurn() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not destroy your land during your own turn")
    void doesNotDestroyYourLandDuringYourOwnTurn() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Mana is available and the land survives until the destruction trigger resolves")
    void destructionWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys the enchantment controller's land tapped during the opponent's turn")
    void destroysYourLandDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each land tapped off turn is destroyed while an untapped land survives")
    void destroysOnlyTheLandsTappedForMana() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        var firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        var secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        var untappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(untappedLand)
                .doesNotContain(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
