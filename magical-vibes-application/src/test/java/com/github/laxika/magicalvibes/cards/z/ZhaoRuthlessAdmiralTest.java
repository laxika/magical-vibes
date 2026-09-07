package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhaoRuthlessAdmiral.class, CruelEdict.class, GrizzlyBears.class})
class ZhaoRuthlessAdmiralTest extends BaseCardTest {

    @Test
    void attackingAddsTwoRedManaUntilEndOfCombat() {
        addReadyZhao();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void sacrificingAnotherPermanentBoostsYourCreaturesUntilEndOfTurn() {
        addReadyZhao();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeAt(player1, bears);

        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).getFirst()))
                .isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).getFirst()))
                .isEqualTo(3);
    }

    @Test
    void sacrificingZhaoDoesNotTriggerTheAbility() {
        Permanent zhao = addReadyZhao();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeAt(player1, zhao);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addReadyZhao() {
        return addCreatureReady(player1, new ZhaoRuthlessAdmiral());
    }

    private void sacrificeAt(Player player, Permanent sacrificed) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player, sacrificed.getId());
        harness.passBothPriorities();
    }
}
