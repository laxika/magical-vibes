package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ForcedMarch.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class,
        HowlingMine.class, Ornithopter.class})
class ForcedMarchTest extends BaseCardTest {

    private void castForcedMarch(int xValue) {
        harness.setHand(player1, List.of(new ForcedMarch()));
        harness.addMana(player1, ManaColor.BLACK, 3 + xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Destroys creatures with mana value X or less and spares bigger creatures")
    void destroysCreaturesWithinManaValueBound() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castForcedMarch(2);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not destroy noncreature permanents")
    void sparesNoncreaturePermanents() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new LlanowarElves());

        castForcedMarch(2);

        harness.assertOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("X=0 destroys only zero-mana-value creatures")
    void xZeroDestroysOnlyZeroManaValueCreatures() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new LlanowarElves());

        castForcedMarch(0);

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Resolves when there are no creatures to destroy")
    void resolvesWithoutCreaturesToDestroy() {
        castForcedMarch(0);

        harness.assertInGraveyard(player1, "Forced March");
    }

    @Test
    @DisplayName("Allows matching creatures to regenerate")
    void allowsMatchingCreaturesToRegenerate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castForcedMarch(2);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible matching creatures survive")
    void indestructibleMatchingCreaturesSurvive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castForcedMarch(2);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
