package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TitaniumMan.class)
class TitaniumManTest extends BaseCardTest {

    private static final String FLYING_MODE = "Titanium Man gains flying until end of turn";
    private static final String DAMAGE_MODE = "Titanium Man deals 1 damage to any target";

    @Test
    @DisplayName("Attacking can grant Titanium Man flying until end of turn")
    void attackingCanGrantFlyingUntilEndOfTurn() {
        Permanent titaniumMan = addReadyTitaniumMan();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, FLYING_MODE);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, titaniumMan, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, titaniumMan, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking can deal 1 damage to any target")
    void attackingCanDealDamageToAnyTarget() {
        addReadyTitaniumMan();
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, DAMAGE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent addReadyTitaniumMan() {
        return addCreatureReady(player1, new TitaniumMan());
    }
}
