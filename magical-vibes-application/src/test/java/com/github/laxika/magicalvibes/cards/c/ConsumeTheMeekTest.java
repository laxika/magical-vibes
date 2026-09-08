package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConsumeTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value 3 or less on both sides")
    void destroysCreaturesWithinManaValueLimit() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CentaurCourser());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Forest());

        castConsumeTheMeek();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Centaur Courser");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroyed creatures cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        castConsumeTheMeek();

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    private void castConsumeTheMeek() {
        harness.setHand(player1, List.of(new ConsumeTheMeek()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0);
    }
}
