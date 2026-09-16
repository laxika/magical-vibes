package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Endurance.class, Forest.class, GrizzlyBears.class})
class EnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts all cards from the target player's graveyard on the bottom of their library")
    void etbPutsTargetGraveyardOnBottom() {
        Card graveyardLand = new Forest();
        Card graveyardCreature = new GrizzlyBears();
        Card existingTop = new Forest();
        harness.setGraveyard(player2, List.of(graveyardLand, graveyardCreature));
        harness.setLibrary(player2, List.of(existingTop));
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(existingTop);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(graveyardLand, graveyardCreature);
    }

    @Test
    @DisplayName("ETB may resolve without a target")
    void etbMayResolveWithoutTarget() {
        Card graveyardCard = new Forest();
        harness.setGraveyard(player2, List.of(graveyardCard));
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        harness.assertOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("Evoke exiles a green card and sacrifices Endurance after its ETB")
    void evokeExilesGreenCardAndSacrificesSelf() {
        Card greenCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new Endurance(), greenCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        gs.playCard(gd, player1, 0, 0, player2.getId(), null,
                List.of(), List.of(), false, null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(greenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Endurance");
        harness.assertNotOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    private void giveHardcastMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
