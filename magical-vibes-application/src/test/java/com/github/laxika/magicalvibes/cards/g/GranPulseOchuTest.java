package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GranPulseOchu.class, Forest.class, Shock.class})
class GranPulseOchuTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each permanent card in its controller's graveyard")
    void boostsForPermanentCardsInControllerGraveyard() {
        Permanent ochu = addReadyOchu(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ochu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ochu)).isEqualTo(3);
    }

    @Test
    @DisplayName("The graveyard count boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ochu = addReadyOchu(player1);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ochu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ochu)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ochu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ochu)).isEqualTo(1);
    }

    private Permanent addReadyOchu(Player player) {
        return addCreatureReady(player, new GranPulseOchu());
    }
}
