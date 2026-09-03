package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tidal Wave")
@CardUsed({TidalWave.class, Boomerang.class})
class TidalWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 Wall token with defender")
    void createsWallToken() {
        harness.castFromHand(player1, new TidalWave(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wall")).hasSize(1);
        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wall.getCard().getPower()).isEqualTo(5);
        assertThat(wall.getCard().getToughness()).isEqualTo(5);
        assertThat(wall.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(wall.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("The Wall token is sacrificed at the beginning of the next end step")
    void wallSacrificedAtNextEndStep() {
        harness.castFromHand(player1, new TidalWave(), "{2}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wall");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Wall");
    }

    @Test
    @DisplayName("The delayed sacrifice can be responded to before it resolves")
    void delayedSacrificeCanBeRespondedTo() {
        harness.castFromHand(player1, new TidalWave(), "{2}{U}");
        harness.passBothPriorities();
        Permanent wall = findPermanent(player1, "Wall");

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Wall");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boomerang");
        assertThat(gd.stack).isEmpty();
    }
}
