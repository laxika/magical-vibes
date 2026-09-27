package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentAncestor.class, ViashinoFangtail.class})
class BenevolentAncestorTest extends BaseCardTest {

    private Permanent addAncestorReady() {
        return addCreatureReady(player1, new BenevolentAncestor());
    }

    private Permanent addFangtailReady() {
        return addCreatureReady(player1, new ViashinoFangtail());
    }

    private void activateAndResolve(Permanent source, UUID targetId) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target player")
    void preventsDamageToPlayer() {
        Permanent ancestor = addAncestorReady();
        Permanent firstFangtail = addFangtailReady();
        Permanent secondFangtail = addFangtailReady();
        harness.setLife(player2, 20);
        activateAndResolve(ancestor, player2.getId());
        activateAndResolve(firstFangtail, player2.getId());
        activateAndResolve(secondFangtail, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target creature")
    void preventsDamageToCreature() {
        Permanent ancestor = addAncestorReady();
        Permanent firstFangtail = addFangtailReady();
        Permanent secondFangtail = addFangtailReady();
        Permanent target = addCreatureReady(player2, new BenevolentAncestor());
        activateAndResolve(ancestor, target.getId());
        activateAndResolve(firstFangtail, target.getId());
        activateAndResolve(secondFangtail, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Benevolent Ancestor")).isSameAs(target);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent ancestor = addAncestorReady();
        Permanent fangtail = addFangtailReady();
        harness.setLife(player2, 20);

        activateAndResolve(ancestor, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        activateAndResolve(fangtail, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
