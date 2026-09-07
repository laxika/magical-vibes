package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthKingdomProtectors.class, HadaFreeblade.class, GrizzlyBears.class})
class EarthKingdomProtectorsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing grants indestructible to another Ally you control")
    void sacrificeGrantsIndestructibleToAnotherAlly() {
        Permanent protectors = addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, 0, null, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(protectors);
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, 0, null, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Only another Ally you control can be targeted")
    void restrictsTargetToAnotherControlledAlly() {
        Permanent protectors = addCreatureReady(player1, new EarthKingdomProtectors());
        Permanent ownAlly = addCreatureReady(player1, new HadaFreeblade());
        Permanent ownNonAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAlly = addCreatureReady(player2, new HadaFreeblade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, protectors.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownNonAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Ally you control");

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
