package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Froghemoth.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class FroghemothTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage sets the exile limit and rewards creature and noncreature cards")
    void combatDamageScalesExileAndRewardsCardTypes() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Shock shock = new Shock();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, giant, shock, remaining));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId(), shock.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(bears, giant, shock);
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Choosing no cards produces no bonuses")
    void choosingNoCardsProducesNoBonuses() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(bears, shock));
        harness.setLife(player1, 10);

        Permanent froghemoth = addAttackingFroghemoth();
        resolveCombat();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears, shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(froghemoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addAttackingFroghemoth() {
        Permanent froghemoth = addCreatureReady(player1, new Froghemoth());
        froghemoth.setAttacking(true);
        return froghemoth;
    }
}
