package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BygoneColossus.class)
class BygoneColossusTest extends BaseCardTest {

    @Test
    void warpCastsAndExilesAtTheNextEndStep() {
        BygoneColossus colossus = new BygoneColossus();
        harness.setHand(player1, List.of(colossus));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(colossus.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(colossus.getId())).isNotNull();
    }
}
