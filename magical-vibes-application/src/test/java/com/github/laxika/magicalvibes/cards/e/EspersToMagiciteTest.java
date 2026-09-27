package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EspersToMagicite.class, GrizzlyBears.class, Plains.class, Shock.class})
class EspersToMagiciteTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent graveyard and copies a chosen creature as an artifact")
    void exilesOpponentsGraveyardsAndCopiesChosenCreatureAsArtifact() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setGraveyard(player2, List.of(bears, shock));

        castEspersToMagicite();
        harness.passBothPriorities();

        PendingInteraction.EspersToMagiciteCreatureChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EspersToMagiciteCreatureChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears, shock);
    }

    @Test
    @DisplayName("May decline the optional creature copy")
    void mayDeclineCreatureCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castEspersToMagicite();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castEspersToMagicite() {
        harness.setHand(player1, List.of(new EspersToMagicite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
