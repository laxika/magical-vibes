package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SanguinePraetor.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class SanguinePraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys all creatures with the same mana value")
    void destroysCreaturesWithSacrificedCreatureManaValue() {
        Permanent praetor = addCreatureReady(player1, new SanguinePraetor());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(praetor), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The source may be sacrificed and its mana value is still used")
    void maySacrificeSource() {
        Permanent source = addCreatureReady(player1, new SanguinePraetor());
        addCreatureReady(player2, new SanguinePraetor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanguine Praetor");
        harness.assertNotOnBattlefield(player1, "Sanguine Praetor");
        harness.assertNotOnBattlefield(player2, "Sanguine Praetor");
        harness.assertInGraveyard(player2, "Sanguine Praetor");
    }
}
