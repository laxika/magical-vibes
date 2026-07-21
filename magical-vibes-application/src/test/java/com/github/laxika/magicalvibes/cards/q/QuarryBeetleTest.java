package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuarryBeetleTest extends BaseCardTest {

    /** Casts Quarry Beetle and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castQuarryBeetle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QuarryBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a targeted land card from graveyard to the battlefield")
    void etbReturnsLandToBattlefield() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("A nonland card in the graveyard is not a legal target")
    void nonlandNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
