package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a land tapped for mana outside its controller's turn")
    void destroysLandTappedOutsideItsControllersTurn() {
        harness.addToBattlefield(player1, new PriceOfGlory());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
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

    private void resolveStackFully() {
        for (int i = 0; i < 4 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
