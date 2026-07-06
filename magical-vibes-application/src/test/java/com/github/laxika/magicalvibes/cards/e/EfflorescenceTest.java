package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EfflorescenceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Without life gained, only adds two +1/+1 counters")
    void withoutLifeGainOnlyCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("If you gained life this turn, also grants trample and indestructible")
    void withLifeGainGrantsKeywords() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }
}
