package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrialError.class, BorosSwiftblade.class, GiantSpider.class, GrizzlyBears.class})
class TrialErrorTest extends BaseCardTest {

    @Test
    @DisplayName("Trial returns creatures blocking or blocked by the target creature")
    void trialReturnsCombatOpponentsOfAttackingTarget() {
        Permanent attacker = addReadyCreature(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        connectBlockerToAttacker(blocker, attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TrialError()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trial returns attackers blocked by the target blocker but not the target")
    void trialReturnsAttackerOfBlockingTarget() {
        Permanent attacker = addReadyCreature(player2, new GiantSpider());
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player1, new GrizzlyBears());
        connectBlockerToAttacker(blocker, attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TrialError()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInHand(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Error counters a multicolored spell")
    void errorCountersMulticoloredSpell() {
        BorosSwiftblade spell = new BorosSwiftblade();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new TrialError()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        harness.assertNotOnBattlefield(player1, "Boros Swiftblade");
    }

    @Test
    @DisplayName("Error cannot target a monocolored spell")
    void errorCannotTargetMonocoloredSpell() {
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new TrialError()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void connectBlockerToAttacker(Permanent blocker, Permanent attacker) {
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
    }
}
