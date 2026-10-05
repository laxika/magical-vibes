package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KillShot.class, AlpineGrizzly.class})
class KillShotTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target attacking creature")
    void destroysAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new AlpineGrizzly());
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Alpine Grizzly");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a target that stops attacking before resolution")
    void targetLeavesCombatBeforeResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new AlpineGrizzly());
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player2, "Kill Shot");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can destroy an attacking creature controlled by the caster")
    void destroysOwnAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new KillShot()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Kill Shot");
    }

    @Test
    @DisplayName("Can destroy a blocked attacking creature without destroying its blocker")
    void destroysBlockedAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new AlpineGrizzly());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        blocker.getBlockingTargetIds().add(attacker.getId());
        attacker.setBlockedThisCombat(true);
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertInGraveyard(player1, "Alpine Grizzly");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
