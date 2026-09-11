package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulDead.class, DiregrafGhoul.class, GrizzlyBears.class, Shock.class})
class VengefulDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when another Zombie dies")
    void triggersWhenAnotherZombieDies() {
        harness.addToBattlefield(player1, new VengefulDead());
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.setLife(player2, 20);

        killWithShock(player1, player2, "Diregraf Ghoul");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A non-Zombie creature you control dying does not trigger")
    void doesNotTriggerForNonZombie() {
        harness.addToBattlefield(player1, new VengefulDead());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Vengeful Dead's own death triggers")
    void triggersWhenItselfDies() {
        harness.addToBattlefield(player1, new VengefulDead());
        harness.setLife(player2, 20);

        killWithShock(player2, player1, "Vengeful Dead");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController,
                               String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
