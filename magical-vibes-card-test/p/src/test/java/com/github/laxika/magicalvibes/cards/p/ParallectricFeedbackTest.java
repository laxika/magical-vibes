package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Frazzle;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallectricFeedback.class, HatchingPlans.class, Gristleback.class, Repeal.class, Frazzle.class})
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
        harness.castAndResolveInstant(player2, 0, plans.getId());

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
        harness.castAndResolveInstant(player2, 0, gristleback.getId());

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
        harness.castAndResolveInstant(player2, 0, repeal.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target its controller's own spell without countering it")
    void canDamageItsOwnController() {
        HatchingPlans plans = new HatchingPlans();
        harness.castFromHand(player1, plans, "{1}{U}");
        harness.setHand(player1, List.of(new ParallectricFeedback()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, plans.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hatching Plans");
    }

    @Test
    @DisplayName("Does not damage a controller when the targeted spell has been countered")
    void doesNotDamageWhenTargetLeavesStack() {
        Gristleback gristleback = new Gristleback();
        harness.castFromHand(player1, gristleback, "{2}{G}");
        harness.setHand(player2, List.of(new ParallectricFeedback()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gristleback.getId());

        harness.setHand(player1, List.of(new Frazzle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, gristleback.getId());
        harness.assertInGraveyard(player1, "Gristleback");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Parallectric Feedback");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent on the battlefield")
    void rejectsBattlefieldPermanent() {
        var plans = harness.addToBattlefieldAndReturn(player1, new HatchingPlans());
        harness.setHand(player1, List.of(new ParallectricFeedback()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plans.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Parallectric Feedback");
    }
}
