package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RimeboundDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinFurrierTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents its combat damage to snow creatures")
    void preventsCombatDamageToSnowCreature() {
        Permanent furrier = readyCreature(player1, new GoblinFurrier());
        Permanent snowCreature = readyCreature(player2, new RimeboundDead());
        furrier.setAttacking(true);
        snowCreature.setBlocking(true);
        snowCreature.addBlockingTarget(0);

        resolveCombatDamage();

        assertThat(snowCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(snowCreature);
        assertThat(furrier.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent its combat damage to nonsnow creatures")
    void doesNotPreventCombatDamageToNonsnowCreature() {
        Permanent furrier = readyCreature(player1, new GoblinFurrier());
        Permanent nonsnowCreature = readyCreature(player2, new GrizzlyBears());
        furrier.setAttacking(true);
        nonsnowCreature.setBlocking(true);
        nonsnowCreature.addBlockingTarget(0);

        resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonsnowCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(furrier.getMarkedDamage()).isEqualTo(2);
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
