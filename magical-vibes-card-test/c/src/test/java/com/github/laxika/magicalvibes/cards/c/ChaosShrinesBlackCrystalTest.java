package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosShrinesBlackCrystal.class, GrizzlyBears.class})
class ChaosShrinesBlackCrystalTest extends BaseCardTest {

    @Test
    void exilesControlledNontokenCreatureThatDies() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new ChaosShrinesBlackCrystal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(crystal.getId()))
                .extracting(card -> card.getId()).containsExactly(bears.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    void upkeepMayReturnTrackedCreatureWithFinalityCounter() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new ChaosShrinesBlackCrystal());
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, crystal.getId());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(crystal.getId())).isEmpty();
    }

    @Test
    void upkeepChoicePreservesFinalityCounterReplacement() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new ChaosShrinesBlackCrystal());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        gd.addToExile(player1.getId(), first, crystal.getId());
        gd.addToExile(player1.getId(), second, crystal.getId());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(crystal.getId()))
                .extracting(card -> card.getId()).containsExactly(second.getId());
    }
}
