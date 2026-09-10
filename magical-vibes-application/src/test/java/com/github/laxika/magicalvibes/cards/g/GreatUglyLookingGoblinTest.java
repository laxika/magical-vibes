package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ClapSnap;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatUglyLookingGoblin.class, ClapSnap.class, GrizzlyBears.class})
class GreatUglyLookingGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Clap! Snap! amasses Goblins 2")
    void adventureAmassesGoblinsTwo() {
        GreatUglyLookingGoblin card = new GreatUglyLookingGoblin();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have menace")
    void counteredControlledCreaturesHaveMenace() {
        addCreatureReady(player1, new GreatUglyLookingGoblin());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An existing Army receives two +1/+1 counters")
    void adventureUsesExistingArmy() {
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        GreatUglyLookingGoblin card = new GreatUglyLookingGoblin();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }
}
