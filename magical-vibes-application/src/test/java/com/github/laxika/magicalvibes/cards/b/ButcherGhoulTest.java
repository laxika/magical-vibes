package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ButcherGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Has undying keyword")
    void hasUndying() {
        Permanent ghoul = addCreatureReady(player1, new ButcherGhoul());

        assertThat(gqs.hasKeyword(gd, ghoul, Keyword.UNDYING)).isTrue();
    }

    @Test
    @DisplayName("Undying returns Butcher Ghoul with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Butcher Ghoul");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(2);
        assertThat(returned.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Butcher Ghoul"));
    }

    @Test
    @DisplayName("Undying does not return Butcher Ghoul when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new ButcherGhoul());
        ghoul.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ghoul.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Butcher Ghoul"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Butcher Ghoul"));
        assertThat(gd.stack).isEmpty();
    }
}
