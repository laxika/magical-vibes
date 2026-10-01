package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.ChameleonBlur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitteringMonstrosity.class, AshcoatBear.class, ChameleonBlur.class})
class SkitteringMonstrosityTest extends BaseCardTest {

    @Test
    void sacrificesItselfWhenControllerCastsCreatureSpell() {
        harness.addToBattlefield(player1, new SkitteringMonstrosity());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Monstrosity");
    }

    @Test
    void putsSacrificeTriggerOnStackWhenControllerCastsCreatureSpell() {
        harness.addToBattlefield(player1, new SkitteringMonstrosity());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Skittering Monstrosity"));
    }

    @Test
    void doesNotSacrificeItselfForNoncreatureSpell() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        harness.castFromHand(player1, new ChameleonBlur(), "{3}{G}");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monstrosity);
    }

    @Test
    void doesNotSacrificeItselfWhenOpponentCastsCreatureSpell() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AshcoatBear(), "{1}{G}");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(monstrosity);
    }
}
