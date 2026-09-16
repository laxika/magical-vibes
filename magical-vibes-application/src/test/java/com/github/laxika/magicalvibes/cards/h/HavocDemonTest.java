package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavocDemon.class, EyeblightsEnding.class, EliteVanguard.class, SwordsToPlowshares.class})
class HavocDemonTest extends BaseCardTest {

    @Test
    void deathTriggerGivesAllCreaturesMinusFiveMinusFive() {
        Permanent ownSurvivor = addSixSix(player1);
        Permanent opposingSurvivor = addSixSix(player2);
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new HavocDemon());

        destroyWithEyeblightsEnding(demon);
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

        destroyWithEyeblightsEnding(demon);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(survivor.getEffectivePower()).isEqualTo(6);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void deathTriggerKillsCreaturesWithToughnessFiveOrLess() {
        harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new HavocDemon());

        destroyWithEyeblightsEnding(demon);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Elite Vanguard");
    }

    @Test
    void exilingHavocDemonDoesNotTriggerItsDeathAbility() {
        Permanent survivor = addSixSix(player1);
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new HavocDemon());

        exileWithSwordsToPlowshares(demon);
        harness.passBothPriorities();

        assertThat(survivor.getEffectivePower()).isEqualTo(6);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(6);
    }

    private Permanent addSixSix(Player player) {
        EliteVanguard card = new EliteVanguard();
        card.setPower(6);
        card.setToughness(6);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void destroyWithEyeblightsEnding(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EyeblightsEnding()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void exileWithSwordsToPlowshares(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
