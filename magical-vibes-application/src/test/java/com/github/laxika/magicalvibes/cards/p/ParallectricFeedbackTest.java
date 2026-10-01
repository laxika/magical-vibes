package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParallectricFeedback.class, HatchingPlans.class, Gristleback.class, Repeal.class})
class ParallectricFeedbackTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the target spell's mana value to its controller")
    void damagesTargetSpellControllerByManaValue() {
        HatchingPlans plans = new HatchingPlans();
        harness.castFromHand(player1, plans, "{1}{U}");

        harness.setHand(player2, List.of(new ParallectricFeedback()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, plans.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target a creature spell")
    void damagesCreatureSpellControllerByManaValue() {
        Gristleback gristleback = new Gristleback();
        harness.castFromHand(player1, gristleback, "{2}{G}");

        harness.setHand(player2, List.of(new ParallectricFeedback()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, gristleback.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Includes the chosen X in the target spell's mana value")
    void includesChosenXInTargetSpellManaValue() {
        var targetPermanent = harness.addToBattlefieldAndReturn(player1, new HatchingPlans());
        Repeal repeal = new Repeal();
        harness.setHand(player1, List.of(repeal));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, targetPermanent.getId());

        harness.setHand(player2, List.of(new ParallectricFeedback()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, repeal.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }
}
