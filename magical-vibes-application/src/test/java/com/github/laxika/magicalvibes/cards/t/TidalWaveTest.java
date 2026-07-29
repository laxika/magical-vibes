package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tidal Wave")
class TidalWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 Wall token with defender")
    void createsWallToken() {
        harness.setHand(player1, List.of(new TidalWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getPower()).isEqualTo(5);
        assertThat(wall.getCard().getToughness()).isEqualTo(5);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("The Wall token is sacrificed at the beginning of the next end step")
    void wallSacrificedAtNextEndStep() {
        harness.setHand(player1, List.of(new TidalWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wall");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wall");
    }
}
