package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.t.TheFirstEruption;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarciFableSinger.class, Narcissism.class, TheFirstEruption.class})
class NarciFableSingerTest extends BaseCardTest {

    @Test
    void drawsWhenYouSacrificeAnEnchantment() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        Permanent narcissism = harness.addToBattlefieldAndReturn(player1, new Narcissism());
        harness.setLibrary(player1, List.of(new TheFirstEruption()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 1, null, findPermanent(player1, "Narci, Fable Singer").getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Narcissism");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("The First Eruption");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(narcissism);
    }

    @Test
    void finalChapterUsesSagaManaValueForLifeLossAndGain() {
        harness.addToBattlefield(player1, new NarciFableSinger());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFirstEruption());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Narcissism()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
