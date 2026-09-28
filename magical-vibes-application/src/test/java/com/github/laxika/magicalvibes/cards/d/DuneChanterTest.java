package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuneChanter.class, Forest.class, GrizzlyBears.class})
class DuneChanterTest extends BaseCardTest {

    @Test
    void controlledLandsBecomeDesertsAndCanProduceAnyColor() {
        addCreatureReady(player1, new DuneChanter());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.effectiveLandTypes(gd, ownForest))
                .containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.DESERT);
        assertThat(gqs.effectiveLandTypes(gd, opponentForest))
                .containsExactly(CardSubtype.FOREST);

        harness.activateAbility(player1, 1, null, null);

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(opponentForest.isTapped()).isFalse();
    }

    @Test
    void ownedLandCardsOutsideTheBattlefieldBecomeDeserts() {
        addCreatureReady(player1, new DuneChanter());
        Forest handForest = new Forest();
        Forest graveyardForest = new Forest();
        Forest libraryForest = new Forest();
        Forest exiledForest = new Forest();
        harness.setHand(player1, List.of(handForest));
        harness.setGraveyard(player1, List.of(graveyardForest));
        harness.setLibrary(player1, List.of(libraryForest));
        harness.setExile(player1, List.of(exiledForest));

        assertThat(gqs.cardHasSubtype(handForest, CardSubtype.DESERT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardForest, CardSubtype.DESERT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryForest, CardSubtype.DESERT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exiledForest, CardSubtype.DESERT, gd, player1.getId())).isTrue();
    }

    @Test
    void millsTwoAndGainsOneLifeForEachLandMilled() {
        addCreatureReady(player1, new DuneChanter());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }
}
