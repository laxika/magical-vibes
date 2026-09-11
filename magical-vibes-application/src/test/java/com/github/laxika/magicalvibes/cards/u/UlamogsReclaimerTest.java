package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsReclaimer.class, LightningBolt.class, PathToExile.class, GrizzlyBears.class})
class UlamogsReclaimerTest extends BaseCardTest {

    @Test
    void returnsTargetInstantAfterPuttingOpponentOwnedExiledCardIntoGraveyard() {
        LightningBolt bolt = new LightningBolt();
        PathToExile exiledCard = new PathToExile();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setExile(player2, List.of(exiledCard));

        castReclaimer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Path to Exile");
    }

    @Test
    void decliningToProcessAnExiledCardDoesNotReturnTheTarget() {
        LightningBolt bolt = new LightningBolt();
        PathToExile exiledCard = new PathToExile();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setExile(player2, List.of(exiledCard));

        castReclaimer();

        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void doesNotReturnACreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setExile(player2, List.of(new PathToExile()));

        castReclaimer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castReclaimer() {
        harness.setHand(player1, List.of(new UlamogsReclaimer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
