package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
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

@CardUsed({RampagingYaoGuai.class, FountainOfYouth.class, PhyrexianArena.class, GloriousAnthem.class})
class RampagingYaoGuaiTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X counters and destroys any number of artifacts and enchantments within X")
    void entersWithCountersAndDestroysTargetsWithinX() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        castYaoGuai(4, List.of(fountain.getId(), arena.getId()));

        Permanent yaoGuai = findPermanent(player1, "Rampaging Yao Guai");
        assertThat(yaoGuai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Fountain of Youth", "Phyrexian Arena");
    }

    @Test
    @DisplayName("Rejects target selections whose total mana value exceeds X")
    void rejectsTargetsOverTotalManaValueLimit() {
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        prepareCast(4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 4, null, null,
                List.of(arena.getId(), anthem.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
    }

    private void castYaoGuai(int xValue, List<java.util.UUID> targetIds) {
        prepareCast(xValue);
        gs.playCard(gd, player1, 0, xValue, null, null, targetIds, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast(int xValue) {
        harness.setHand(player1, List.of(new RampagingYaoGuai()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
