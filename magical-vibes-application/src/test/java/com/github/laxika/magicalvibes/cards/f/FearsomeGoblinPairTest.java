package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearsomeGoblinPair.class, Murder.class, GrizzlyBears.class})
class FearsomeGoblinPairTest extends BaseCardTest {

    @Test
    @DisplayName("When Fearsome Goblin Pair dies, it amasses Goblins 4 without an Army")
    void deathTriggerCreatesGoblinArmy() {
        Permanent pair = harness.addToBattlefieldAndReturn(player1, new FearsomeGoblinPair());

        destroyPair(pair.getId());

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getEffectivePower()).isEqualTo(4);
        assertThat(army.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("When Fearsome Goblin Pair dies, it amasses Goblins 4 on an existing Army")
    void deathTriggerAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent pair = harness.addToBattlefieldAndReturn(player1, new FearsomeGoblinPair());

        destroyPair(pair.getId());

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    private void destroyPair(UUID pairId) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, pairId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
