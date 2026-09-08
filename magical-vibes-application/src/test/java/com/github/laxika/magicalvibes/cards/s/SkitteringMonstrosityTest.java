package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitteringMonstrosity.class, GrizzlyBears.class, Opt.class})
class SkitteringMonstrosityTest extends BaseCardTest {

    @Test
    void sacrificesItselfWhenControllerCastsCreatureSpell() {
        harness.addToBattlefield(player1, new SkitteringMonstrosity());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Monstrosity");
    }

    @Test
    void doesNotSacrificeItselfForNoncreatureSpell() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monstrosity);
    }

    @Test
    void doesNotSacrificeItselfWhenOpponentCastsCreatureSpell() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monstrosity);
    }
}
