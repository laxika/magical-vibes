package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MartyrsBond.class, GrizzlyBears.class, FountainOfYouth.class,
        AngelicChorus.class, Shock.class})
class MartyrsBondTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a permanent sharing the dead permanent's card type")
    void opponentSacrificesMatchingPermanentType() {
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new MartyrsBond());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dyingCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The Bond triggers when it is put into a graveyard")
    void sourceDeathTriggers() {
        Permanent bond = harness.addToBattlefieldAndReturn(player1, new MartyrsBond());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bond));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Martyr's Bond");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
