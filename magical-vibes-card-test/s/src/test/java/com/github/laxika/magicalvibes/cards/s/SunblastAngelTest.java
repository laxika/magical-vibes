package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SunblastAngel.class, GrizzlyBears.class, LlanowarElves.class, DarksteelMyr.class, GoldMyr.class, Plains.class})
class SunblastAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys tapped creatures on both sides")
    void etbDestroysTappedCreaturesOnBothSides() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).tap();

        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("ETB does not destroy untapped creatures")
    void etbDoesNotDestroyUntappedCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sunblast Angel itself is untapped so does not destroy itself")
    void doesNotDestroyItself() {
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunblast Angel");
    }

    @Test
    @DisplayName("Indestructible tapped creature survives ETB")
    void indestructibleTappedCreatureSurvives() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped creature with regeneration shield can be regenerated")
    void tappedCreatureCanBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        // Resolve the creature spell and put its trigger on the stack.
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        // Creature should survive via regeneration since cannotBeRegenerated is false
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature tapped after the Angel enters is destroyed at resolution")
    void creatureTappedInResponseIsDestroyed() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        myr.setSummoningSick(false);
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.tapPermanent(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertInGraveyard(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Creature untapped before the trigger resolves survives")
    void creatureUntappedBeforeResolutionSurvives() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        myr.tap();
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        harness.passBothPriorities();

        myr.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gold Myr");
        harness.assertNotInGraveyard(player2, "Gold Myr");
    }

    @Test
    @DisplayName("The Angel is also destroyed if tapped before its trigger resolves")
    void tappedAngelDestroysItself() {
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        harness.passBothPriorities();

        findPermanent(player1, "Sunblast Angel").tap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sunblast Angel");
        harness.assertInGraveyard(player1, "Sunblast Angel");
    }

    @Test
    @DisplayName("Tapped noncreatures survive while tapped creatures are destroyed")
    void tappedNoncreatureSurvives() {
        harness.addToBattlefieldAndReturn(player2, new Plains()).tap();
        harness.addToBattlefieldAndReturn(player2, new GoldMyr()).tap();
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotInGraveyard(player2, "Plains");
        harness.assertInGraveyard(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Printed indestructibility saves a tapped artifact creature")
    void printedIndestructibilityPreventsDestruction() {
        harness.addToBattlefieldAndReturn(player2, new DarksteelMyr()).tap();
        harness.addToBattlefieldAndReturn(player2, new GoldMyr()).tap();
        harness.castFromHand(player1, new SunblastAngel(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
        harness.assertInGraveyard(player2, "Gold Myr");
    }
}
