package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CausticRain.class, Plains.class, GrizzlyBears.class})
class CausticRainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Caustic Rain exiles target land")
    void exilesTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Plains"));
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertNotInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Can exile your own land")
    void exilesOwnLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Plains"));
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
