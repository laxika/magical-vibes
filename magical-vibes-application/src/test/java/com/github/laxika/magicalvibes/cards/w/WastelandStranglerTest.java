package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WastelandStrangler.class, GrizzlyBears.class, PathToExile.class})
class WastelandStranglerTest extends BaseCardTest {

    @Test
    void processesAnExiledCardAndGivesTargetCreatureMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void decliningToProcessAnExiledCardDoesNotGiveMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    private void castWastelandStrangler() {
        harness.setHand(player1, List.of(new WastelandStrangler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
