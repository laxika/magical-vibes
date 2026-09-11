package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlastedLandscape.class, CoralMerfolk.class})
class BlastedLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingProducesColorlessMana() {
        Permanent landscape = harness.addToBattlefieldAndReturn(player1, new BlastedLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(landscape.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BlastedLandscape()));
        harness.setLibrary(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blasted Landscape");
        harness.assertInHand(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Cycling cannot be activated without two generic mana")
    void cyclingRequiresTwoGenericMana() {
        BlastedLandscape landscape = new BlastedLandscape();
        harness.setHand(player1, List.of(landscape));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landscape);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
