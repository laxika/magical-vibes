package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HieroglyphicIllumination.class, DoomedDissenter.class})
class HieroglyphicIlluminationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws two cards")
    void drawsTwoCards() {
        harness.setHand(player1, List.of(new HieroglyphicIllumination()));
        harness.setLibrary(player1, List.of(new DoomedDissenter(), new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hieroglyphic Illumination");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new HieroglyphicIllumination()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hieroglyphic Illumination");
        harness.assertInHand(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Cycling pays its costs before drawing on resolution")
    void cyclingPaysCostsBeforeDrawing() {
        harness.setHand(player1, List.of(new HieroglyphicIllumination()));
        harness.setLibrary(player1, List.of(new DoomedDissenter(), new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hieroglyphic Illumination");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Doomed Dissenter");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with nonblue mana and does not discard on failure")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new HieroglyphicIllumination()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Hieroglyphic Illumination");
        harness.assertNotInGraveyard(player1, "Hieroglyphic Illumination");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The nonactive player can cast the instant and draws from their own library")
    void nonactivePlayerDrawsTwoCards() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HieroglyphicIllumination()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.setLibrary(player2, List.of(new DoomedDissenter(), new DoomedDissenter()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Hieroglyphic Illumination");
    }
}
