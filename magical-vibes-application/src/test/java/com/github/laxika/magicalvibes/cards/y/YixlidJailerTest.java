package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YixlidJailer.class, AvenInitiate.class, ThinkTwice.class})
class YixlidJailerTest extends BaseCardTest {

    @Test
    void preventsActivatingAbilitiesOfCardsInGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new AvenInitiate()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    @Test
    void preventsFlashbackFromGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
