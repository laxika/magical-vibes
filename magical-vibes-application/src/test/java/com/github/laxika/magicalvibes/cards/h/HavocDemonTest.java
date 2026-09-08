package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({HavocDemon.class, Murder.class, GrizzlyBears.class})
class HavocDemonTest extends BaseCardTest {

    @Test
    void deathTriggerGivesAllCreaturesMinusFiveMinusFive() {
        Permanent ownSurvivor = addSixSix(player1);
        Permanent opposingSurvivor = addSixSix(player2);
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new HavocDemon());

        destroyWithMurder(demon);
        harness.passBothPriorities();

        assertThat(ownSurvivor.getEffectivePower()).isEqualTo(1);
        assertThat(ownSurvivor.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingSurvivor.getEffectivePower()).isEqualTo(1);
        assertThat(opposingSurvivor.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void deathTriggerLastsUntilEndOfTurn() {
        Permanent survivor = addSixSix(player1);
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new HavocDemon());

        destroyWithMurder(demon);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(survivor.getEffectivePower()).isEqualTo(6);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(6);
    }

    private Permanent addSixSix(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(6);
        card.setToughness(6);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void destroyWithMurder(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
