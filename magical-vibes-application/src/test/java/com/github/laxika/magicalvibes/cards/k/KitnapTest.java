package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Kitnap.class, GrizzlyBears.class, FountainOfYouth.class})
class KitnapTest extends BaseCardTest {

    @Test
    void withoutGiftTapsControlsAndStunsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(bear, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void promisedGiftDrawsForOpponentAndSkipsStunCounters() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(bear, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void canEnchantOnlyCreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Kitnap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, artifact.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target, boolean giftPromised) {
        harness.setHand(player1, List.of(new Kitnap()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstantWithGift(player1, 0, target.getId(), giftPromised);
        harness.passBothPriorities();
    }
}
