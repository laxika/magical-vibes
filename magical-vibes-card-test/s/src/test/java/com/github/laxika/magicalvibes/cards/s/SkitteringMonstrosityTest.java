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

    @Test
    void doesNotTriggerOnItsOwnCast() {
        harness.castFromHand(player1, new SkitteringMonstrosity(), "{3}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skittering Monstrosity");
        harness.assertNotInGraveyard(player1, "Skittering Monstrosity");
    }

    @Test
    void sacrificesBeforeTheCreatureSpellResolves() {
        harness.addToBattlefield(player1, new SkitteringMonstrosity());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        harness.assertOnBattlefield(player1, "Skittering Monstrosity");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skittering Monstrosity");
        harness.assertInGraveyard(player1, "Skittering Monstrosity");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    void eachMonstrositySacrificesItselfForTheSameCreatureSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkitteringMonstrosity());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard());
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }
}
