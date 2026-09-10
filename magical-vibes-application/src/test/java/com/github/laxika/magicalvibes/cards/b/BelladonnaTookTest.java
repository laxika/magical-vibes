package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelladonnaTook.class, GrizzlyBears.class, KrenkosCommand.class})
class BelladonnaTookTest extends BaseCardTest {

    @Test
    @DisplayName("Token-entry resolutions gain life, draw, then put counters on creatures")
    void tokenEntryResolutionsProgressThroughAllThreeEffects() {
        harness.addToBattlefield(player1, new BelladonnaTook());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new KrenkosCommand(), new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin")).allSatisfy(goblin ->
                assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }
}
