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
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
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
}
