package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrakusethMawOfFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Attack deals 4 damage to the first target and 3 to each of two other targets")
    void attackDealsFourAndThreeDamage() {
        addCreatureReady(player1, new DrakusethMawOfFlames());
        Permanent firstTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent secondTarget = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent thirdTarget = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, thirdTarget.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(4);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(thirdTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack can choose a player for 4 damage and decline the other targets")
    void attackCanChoosePlayerAndNoOtherTargets() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DrakusethMawOfFlames());
        addCreatureReady(player2, new ColossalDreadmaw());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
