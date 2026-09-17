package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChromeshellCrab;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.p.PsionicBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Willbender.class, PsionicBlast.class, ProdigalSorcerer.class, ChromeshellCrab.class})
class WillbenderTest extends BaseCardTest {

    @Test
    void turningFaceUpRetargetsSingleTargetSpell() {
        Permanent willbender = castFaceDownWillbender();
        PsionicBlast psionicBlast = new PsionicBlast();
        harness.setHand(player1, List.of(psionicBlast));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        turnFaceUp(willbender);
        resolveRetargetingTo(psionicBlast.getId(), player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void turningFaceUpRetargetsSingleTargetAbility() {
        Permanent willbender = castFaceDownWillbender();
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer),
                null, player2.getId());
        harness.passPriority(player1);

        turnFaceUp(willbender);
        resolveRetargetingTo(sorcerer.getCard().getId(), player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void turningFaceUpCannotTargetAbilityWithMultipleTargets() {
        Permanent willbender = castFaceDownWillbender();
        Permanent ownTarget = addCreatureReady(player1, new Willbender());
        Permanent opponentTarget = addCreatureReady(player2, new ChromeshellCrab());
        Permanent crab = castFaceDownChromeshellCrab();

        turnFaceUpChromeshellCrab(crab);
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.handlePermanentChosen(player1, opponentTarget.getId());

        turnFaceUp(willbender);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownTarget);
    }

    private Permanent castFaceDownWillbender() {
        harness.setHand(player2, List.of(new Willbender()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player2, "Willbender");
    }

    private void turnFaceUp(Permanent willbender) {
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(willbender));
    }

    private void turnFaceUpChromeshellCrab(Permanent crab) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crab));
    }

    private Permanent castFaceDownChromeshellCrab() {
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Chromeshell Crab");
    }

    private void resolveRetargetingTo(java.util.UUID stackTargetId, java.util.UUID newTargetId) {
        harness.handlePermanentChosen(player2, stackTargetId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, newTargetId);
        harness.passBothPriorities();
    }

}
