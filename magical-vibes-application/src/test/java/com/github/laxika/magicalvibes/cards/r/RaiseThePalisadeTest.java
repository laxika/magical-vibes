package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({RaiseThePalisade.class, AvianChangeling.class, DaruLancer.class, GrizzlyBears.class, Plains.class})
class RaiseThePalisadeTest extends BaseCardTest {

    @Test
    void returnsCreaturesNotOfChosenTypeFromAllBattlefields() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new DaruLancer());
        harness.addToBattlefield(player1, new Plains());

        cast();
        harness.handleListChoice(player1, "BEAR");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Daru Lancer");
        harness.assertInHand(player2, "Daru Lancer");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void cast() {
        harness.setHand(player1, List.of(new RaiseThePalisade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
