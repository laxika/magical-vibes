package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarnageGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("A blocking creature's controller loses 1 life")
    void blockerControllerLosesLife() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new CarnageGladiator());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each blocking creature triggers separately")
    void triggersOncePerBlocker() {
        Permanent attacker1 = addReady(player1, new HillGiant());
        attacker1.setAttacking(true);
        Permanent attacker2 = addReady(player1, new HillGiant());
        attacker2.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        addReady(player1, new CarnageGladiator());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveStack();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The trigger is symmetric — its own controller loses life when they block")
    void ownControllerLosesLifeWhenBlocking() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new CarnageGladiator());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveStack();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No blockers declared means no life loss")
    void noBlockersNoLifeLoss() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player1, new CarnageGladiator());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}{B}{R} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        addReady(player1, new CarnageGladiator());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent gladiator = findPermanent(player1, "Carnage Gladiator");
        assertThat(gladiator.getRegenerationShield()).isEqualTo(1);
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
