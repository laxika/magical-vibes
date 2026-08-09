package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlagueDogsTest extends BaseCardTest {

    @Test
    @DisplayName("When Plague Dogs dies, all creatures get -1/-1 until end of turn")
    void deathTriggerDebuffsAllCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new PlagueDogs());
        harness.addToBattlefield(player1, new HillGiant());
        HillGiant blockerCard = new HillGiant();
        blockerCard.setPower(5);
        blockerCard.setToughness(5);
        Permanent blocker = new Permanent(blockerCard);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent dogs = findPermanent(player1, "Plague Dogs");
        dogs.setSummoningSick(false);
        dogs.setAttacking(true);

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hill Giant").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Hill Giant").getEffectiveToughness()).isEqualTo(2);
        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hill Giant").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Hill Giant").getEffectiveToughness()).isEqualTo(3);
        assertThat(blocker.getEffectivePower()).isEqualTo(5);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing Plague Dogs draws a card")
    void sacrificeAbilityDrawsCard() {
        addReadyPlagueDogs(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plague Dogs");
        harness.assertInGraveyard(player1, "Plague Dogs");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyPlagueDogs(Player player) {
        Permanent permanent = new Permanent(new PlagueDogs());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
