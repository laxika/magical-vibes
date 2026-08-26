package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndenturedOaf.class, GrizzlyBears.class, HillGiant.class})
class IndenturedOafTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents its combat damage to red creatures")
    void preventsCombatDamageToRedCreature() {
        Permanent oaf = readyCreature(player1, new IndenturedOaf());
        Permanent redCreature = readyCreature(player2, new HillGiant());
        oaf.setAttacking(true);
        redCreature.setBlocking(true);
        redCreature.addBlockingTarget(0);

        resolveCombatDamage();

        assertThat(redCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(redCreature);
        assertThat(oaf.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals combat damage to non-red creatures")
    void dealsCombatDamageToNonRedCreature() {
        Permanent oaf = readyCreature(player1, new IndenturedOaf());
        Permanent nonRedCreature = readyCreature(player2, new GrizzlyBears());
        oaf.setAttacking(true);
        nonRedCreature.setBlocking(true);
        nonRedCreature.addBlockingTarget(0);

        resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonRedCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(oaf.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
