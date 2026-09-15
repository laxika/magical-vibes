package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FadeFromMemory.class, Forest.class})
class FadeFromMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card from a graveyard")
    void exilesTargetCardFromGraveyard() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.setHand(player1, List.of(new FadeFromMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, forest.getId());

        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Can exile a noncreature card from its controller's graveyard")
    void exilesNoncreatureCardFromOwnGraveyard() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new FadeFromMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, forest.getId());

        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new FadeFromMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FadeFromMemory()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fade from Memory");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does nothing if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.setHand(player1, List.of(new FadeFromMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, forest.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(forest);
    }
}
