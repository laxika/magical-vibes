package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PestControl.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, Memnite.class,
        DarksteelAxe.class, SylvokLifestaff.class})
class PestControlTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonland permanents with mana value 1 or less")
    void destroysMatchingPermanentsAcrossBothBattlefields() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new PestControl(), "{W}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Memnite");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling discards Pest Control and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PestControl()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pest Control");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys noncreature artifacts but spares indestructible permanents")
    void destroysNoncreatureArtifactsAndRespectsIndestructible() {
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.addToBattlefield(player2, new SylvokLifestaff());
        harness.addToBattlefield(player1, new DarksteelAxe());
        harness.addToBattlefield(player2, new DarksteelAxe());

        harness.castFromHand(player1, new PestControl(), "{W}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sylvok Lifestaff");
        harness.assertInGraveyard(player2, "Sylvok Lifestaff");
        harness.assertOnBattlefield(player1, "Darksteel Axe");
        harness.assertOnBattlefield(player2, "Darksteel Axe");
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only when the ability resolves")
    void cyclingDiscardsBeforeDrawingWithoutDestroyingPermanents() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new SylvokLifestaff());
        harness.setHand(player1, List.of(new PestControl()));
        harness.setLibrary(player1, List.of(new Memnite()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Pest Control");
        harness.assertNotInHand(player1, "Pest Control");
        harness.assertNotInHand(player1, "Memnite");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Memnite");
        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertOnBattlefield(player2, "Sylvok Lifestaff");
    }
}
