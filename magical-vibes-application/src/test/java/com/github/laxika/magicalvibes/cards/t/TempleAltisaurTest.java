package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempleAltisaurTest extends BaseCardTest {

    @Test
    void reducesDamageToAnotherDinosaurYouControlToOne() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new PygmyAllosaurus());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, dinosaur.getId());
        harness.passBothPriorities();

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotPreventDamageToTempleAltisaurItself() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleAltisaur());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, temple.getId());
        harness.passBothPriorities();

        assertThat(temple.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageToNonDinosaurCreatureYouControl() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void reducesCombatDamageToAnotherDinosaurYouControlToOne() {
        harness.addToBattlefield(player1, new TempleAltisaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new PygmyAllosaurus());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        dinosaur.setSummoningSick(false);
        dinosaur.setBlocking(true);
        dinosaur.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dinosaur.getMarkedDamage()).isEqualTo(1);
    }
}
