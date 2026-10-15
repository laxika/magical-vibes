package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.c.CloisteredYouth;
import com.github.laxika.magicalvibes.cards.t.ThrabenSentry;
import com.github.laxika.magicalvibes.cards.v.VillageBellRinger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Moonmist.class, GatstafShepherd.class, WalkingCorpse.class, DarkthicketWolf.class,
        CloisteredYouth.class, ThrabenSentry.class, VillageBellRinger.class})
class MoonmistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Moonmist puts it on the stack as an instant")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Transforms Human DFC creatures when it resolves")
    void transformsHumanDfcCreatures() {
        // GatstafShepherd is a Human Werewolf DFC
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        assertThat(shepherd.isTransformed()).isFalse();

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    @Test
    @DisplayName("Transforms Human DFCs on both players' battlefields")
    void transformsHumansOnBothBattlefields() {
        Permanent p1Shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());
        Permanent p2Shepherd = harness.addToBattlefieldAndReturn(player2, new GatstafShepherd());

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(p1Shepherd.isTransformed()).isTrue();
        assertThat(p2Shepherd.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform a back face that is no longer Human")
    void doesNotTransformNonHumanBackFace() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // Manually transform to back face (Gatstaf Howler is a Werewolf, no longer Human)
        Card backFace = shepherd.getOriginalCard().getBackFaceCard();
        shepherd.setCard(backFace);
        shepherd.setTransformed(true);
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");

        // Moonmist says "Transform all Humans" — Gatstaf Howler is a Werewolf (not Human)
        // so it should NOT be affected
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        // Should stay transformed since Gatstaf Howler is not a Human
        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    @Test
    @DisplayName("Does not affect non-Human creatures")
    void doesNotAffectNonHumans() {
        // WalkingCorpse is a Zombie, not a Human
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(corpse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Prevents non-Wolf combat damage while Wolves and Werewolves deal damage")
    void preventsOnlyNonWolfCombatDamage() {
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());
        Permanent wolf = addCreatureReady(player1, new DarkthicketWolf());
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        corpse.setAttacking(true);
        wolf.setAttacking(true);
        shepherd.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player2, 20);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Non-Werewolf/Wolf creatures are prevented from dealing combat damage")
    void nonWerewolfCreaturesPreventedFromDealingCombatDamage() {
        // WalkingCorpse is a Zombie — not a Werewolf or Wolf
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, corpse, true)).isTrue();
    }

    @Test
    @DisplayName("Non-Werewolf/Wolf creatures can still deal non-combat damage")
    void nonWerewolfCreaturesCanDealNonCombatDamage() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        // Non-combat damage should not be prevented
        assertThat(gqs.isPreventedFromDealingDamage(gd, corpse, false)).isFalse();
    }

    @Test
    @DisplayName("Werewolf creatures are NOT prevented from dealing combat damage")
    void werewolfCreaturesCanDealCombatDamage() {
        // GatstafShepherd front face is Human Werewolf — has Werewolf subtype
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        // After Moonmist, the shepherd transformed to Gatstaf Howler (Werewolf subtype)
        // Should be able to deal combat damage
        assertThat(gqs.isPreventedFromDealingDamage(gd, shepherd, true)).isFalse();
    }

    @Test
    @DisplayName("Moonmist goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Moonmist");
    }

    @Test
    void transformsNonWerewolfHumanAndPreventsItsCombatDamage() {
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(youth.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Unholy Fiend");
        assertThat(gqs.isPreventedFromDealingDamage(gd, youth, true)).isTrue();
    }

    @Test
    void transformsHumanBackFaceToFrontFace() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new ThrabenSentry());
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(sentry.isTransformed()).isTrue();

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(sentry.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Thraben Sentry");
    }

    @Test
    void singleFacedHumanRemainsOnBattlefield() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new VillageBellRinger());
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(human.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Village Bell-Ringer");
        assertThat(gqs.isPreventedFromDealingDamage(gd, human, true)).isTrue();
    }

    @Test
    void preventionAppliesToCreaturesEnteringAfterResolutionOnEitherSide() {
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());

        assertThat(gqs.isPreventedFromDealingDamage(gd, corpse, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, wolf, true)).isFalse();
    }

    @Test
    void preventionExpiresOnFollowingTurn() {
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent corpse = addCreatureReady(player2, new WalkingCorpse());
        corpse.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player1, 20);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }
}
