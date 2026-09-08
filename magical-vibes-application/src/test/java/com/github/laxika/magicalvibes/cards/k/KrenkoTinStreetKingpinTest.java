package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KrenkoTinStreetKingpin.class)
class KrenkoTinStreetKingpinTest extends BaseCardTest {

    @Test
    void attackingPutsCounterOnKrenkoAndCreatesTokensEqualToItsNewPower() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(krenko.getEffectivePower()).isEqualTo(2);
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    void tokenCountUsesPowerAfterExistingCounters() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());
        krenko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(krenko.getEffectivePower()).isEqualTo(4);
        assertThat(findPermanents(player1, "Goblin")).hasSize(4);
    }
}
