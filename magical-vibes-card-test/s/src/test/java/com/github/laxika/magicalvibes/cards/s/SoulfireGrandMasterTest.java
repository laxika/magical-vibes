package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BatheInDragonfire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornetSting;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WildSlash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SoulfireGrandMaster.class, GrizzlyBears.class, HornetSting.class, Shock.class,
        WildSlash.class, BatheInDragonfire.class, TurnToFrog.class, Negate.class})
class SoulfireGrandMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells of any color you control have lifelink")
    void spellsOfAnyColorHaveLifelink() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new HornetSting()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Activated ability returns the next hand-cast instant or sorcery to hand")
    void returnsNextHandCastSpellToHand() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Return-to-hand rider waits past a non-instant or non-sorcery spell")
    void waitsForInstantOrSorcerySpell() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void sorceryDamageHasLifelinkEvenWhenItKillsSoulfire() {
        var soulfire = harness.addToBattlefieldAndReturn(player1, new SoulfireGrandMaster());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, soulfire.getId());

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Soulfire Grand Master");
    }

    @Test
    void opponentsSpellsDoNotGainLifelink() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleSoulfiresDoNotMultiplyLifelink() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 22);
    }

    @Test
    void losingAllAbilitiesStopsGrantingSpellLifelink() {
        var soulfire = harness.addToBattlefieldAndReturn(player1, new SoulfireGrandMaster());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WildSlash()));
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, soulfire.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void returnEffectSurvivesSourceLeavingBeforeAbilityResolves() {
        var soulfire = harness.addToBattlefieldAndReturn(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player2, 0, soulfire.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Soulfire Grand Master");
        harness.assertInHand(player1, "Wild Slash");
        harness.assertNotInGraveyard(player1, "Wild Slash");
    }

    @Test
    void returnsHandCastSorceryAfterResolvingItsEffects() {
        var soulfire = harness.addToBattlefieldAndReturn(player1, new SoulfireGrandMaster());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, soulfire.getId());

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Soulfire Grand Master");
        harness.assertInHand(player1, "Bathe in Dragonfire");
        harness.assertNotInGraveyard(player1, "Bathe in Dragonfire");
    }

    @Test
    void activationDoesNotApplyToSpellAlreadyOnStack() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild Slash");
        harness.assertNotInHand(player1, "Wild Slash");
    }

    @Test
    void returnAppliesToFirstCastSpellRatherThanFirstResolvingSpell() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new Shock(), new WildSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Wild Slash");
        harness.assertNotInHand(player1, "Wild Slash");
    }

    @Test
    void multipleActivationsAllApplyToSameNextSpell() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertInHand(player1, "Wild Slash");

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Wild Slash");
        harness.assertNotInHand(player1, "Wild Slash");
    }

    @Test
    void spellWithIllegalTargetConsumesReturnEffect() {
        var soulfire = harness.addToBattlefieldAndReturn(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new WildSlash(), new Shock()));
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, soulfire.getId());
        harness.castAndResolveInstant(player2, 0, soulfire.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Wild Slash");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInHand(player1, "Shock");
    }

    @Test
    void counteredSpellConsumesReturnEffect() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        var firstSpell = new WildSlash();
        harness.setHand(player1, List.of(firstSpell, new Shock()));
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, firstSpell.getId());
        harness.assertInGraveyard(player1, "Wild Slash");
        harness.assertNotInHand(player1, "Wild Slash");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInHand(player1, "Shock");
    }
}
