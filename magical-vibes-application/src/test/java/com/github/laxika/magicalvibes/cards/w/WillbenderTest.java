package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Willbender.class, LavaAxe.class, ProdigalSorcerer.class})
class WillbenderTest extends BaseCardTest {

    @Test
    void turningFaceUpRetargetsSingleTargetSpell() {
        Permanent willbender = castFaceDownWillbender();
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);

        turnFaceUp(willbender);
        resolveRetargetingTo(lavaAxe.getId(), player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void turningFaceUpRetargetsSingleTargetAbility() {
        Permanent willbender = castFaceDownWillbender();
        Permanent sorcerer = addReadySorcerer(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(player1, sorcerer), null, player2.getId());
        harness.passPriority(player1);

        turnFaceUp(willbender);
        resolveRetargetingTo(sorcerer.getCard().getId(), player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
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

    private void resolveRetargetingTo(java.util.UUID stackTargetId, java.util.UUID newTargetId) {
        harness.handlePermanentChosen(player2, stackTargetId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, newTargetId);
        harness.passBothPriorities();
    }

    private Permanent addReadySorcerer(Player player) {
        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sorcerer);
        return sorcerer;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
