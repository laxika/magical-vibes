package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.ItemShopkeep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CombatTutorial.class, ItemShopkeep.class})
class CombatTutorialTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and a controlled creature gets a +1/+1 counter")
    void drawsAndCountersControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can resolve with no creature target")
    void canResolveWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotOmitRequiredPlayerTarget() {
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTwoControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetSelfAndOmitAnAvailableCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillDrawsWhenCreatureTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillDrawsButDoesNotCounterCreatureNowControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        harness.setHand(player1, List.of(new CombatTutorial()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
