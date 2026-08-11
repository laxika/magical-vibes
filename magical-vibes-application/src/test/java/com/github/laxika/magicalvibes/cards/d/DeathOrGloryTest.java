package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathOrGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Controller separates creature cards and opponent chooses the pile to exile")
    void opponentChoosesPileToExile() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(bears, elves, artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        PendingInteraction.MayAbilityChoice pileChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(pileChoice).isNotNull();
        assertThat(pileChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Choosing the second pile to exile returns the first pile")
    void opponentChoosesSecondPileToExile() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elves);
    }
}
