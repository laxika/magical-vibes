package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KillShotTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target attacking creature")
    void destroysAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }
}
