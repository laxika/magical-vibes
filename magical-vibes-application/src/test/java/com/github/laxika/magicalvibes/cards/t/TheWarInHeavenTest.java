package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWarInHeaven.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class TheWarInHeavenTest extends BaseCardTest {

    @Test
    void chapterOneDrawsThreeAndLosesThreeLife() {
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player1, List.of(new TheWarInHeaven()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void chapterThreeReturnsUpToThreeCreaturesWithinManaValueAndMakesThemArtifactNecrodermisCreatures() {
        Card bears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, secondBears, hillGiant));

        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWarInHeaven());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.maxTotalManaValue()).isEqualTo(8);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), secondBears.getId(), hillGiant.getId()));
        harness.passBothPriorities();

        List<Permanent> returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> List.of("Grizzly Bears", "Hill Giant")
                        .contains(permanent.getCard().getName()))
                .toList();
        assertThat(returned).hasSize(3);
        assertThat(returned).allSatisfy(permanent -> {
            assertThat(gqs.isArtifact(gd, permanent)).isTrue();
            assertThat(permanent.getCounterCount(CounterType.NECRODERMIS)).isEqualTo(1);
        });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears")
                        || card.getName().equals("Hill Giant"));
    }
}
