package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FacetReader.class, Forest.class, GrizzlyBears.class})
class FacetReaderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then discards a card")
    void drawsThenDiscards() {
        Permanent reader = addReadyReader(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(reader.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        addReadyReader(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can discard the card just drawn")
    void canDiscardDrawnCard() {
        addReadyReader(player1);
        Forest kept = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Can activate with an empty hand and must discard the drawn card")
    void canActivateWithEmptyHand() {
        addReadyReader(player1);
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent reader = addReadyReader(player1);
        reader.tap();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent reader = addReadyReader(player1);
        reader.setSummoningSick(true);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(reader.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyReader(Player player) {
        return addCreatureReady(player, new FacetReader());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
