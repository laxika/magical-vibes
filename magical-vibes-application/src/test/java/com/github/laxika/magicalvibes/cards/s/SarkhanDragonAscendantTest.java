package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SarkhanDragonAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Beholding a Dragon you control creates a Treasure")
    void beholdDragonPermanentCreatesTreasure() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        harness.setHand(player1, List.of(new SarkhanDragonAscendant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getCard().getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Beholding a Dragon card from hand creates a Treasure")
    void beholdDragonCardInHandCreatesTreasure() {
        ShivanDragon dragon = new ShivanDragon();
        harness.setHand(player1, List.of(new SarkhanDragonAscendant(), dragon));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A Dragon entering adds a counter, Dragon subtype, and flying until end of turn")
    void dragonEntryBoostsSarkhanUntilEndOfTurn() {
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanDragonAscendant());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, sarkhan)).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, sarkhan)).doesNotContain(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isFalse();
    }
}
