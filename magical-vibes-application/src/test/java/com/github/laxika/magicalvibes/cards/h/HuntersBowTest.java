package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntersBow.class, GrizzlyBears.class, HillGiant.class, Shock.class, Naturalize.class, GiantGrowth.class})
class HuntersBowTest extends BaseCardTest {

    @Test
    void entersAttachedAndDealsDamageEqualToEquippedCreaturesPower() {
        Permanent equippedCreature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new HillGiant());

        castBow(equippedCreature.getId(), victim.getId());
        resolveAllTriggers();

        Permanent bow = findPermanent(player1, "Hunter's Bow");
        assertThat(bow.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canEnterWithoutChoosingTheOptionalDamageTarget() {
        Permanent equippedCreature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new HillGiant());

        castBow(equippedCreature.getId());
        resolveAllTriggers();

        Permanent bow = findPermanent(player1, "Hunter's Bow");
        assertThat(bow.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    void equippedCreatureGetsReachAndWard() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new HuntersBow());
        bow.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.REACH)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, equippedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(equippedCreature.getMarkedDamage()).isZero();
    }

    @Test
    void equipAttachesToAnotherCreatureYouControl() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new HuntersBow());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new HuntersBow());
        bow.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void wardDoesNotCounterControllersOwnSpell() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new HuntersBow());
        bow.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void reequippingMovesReachAndWardWithoutDealingDamage() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new HuntersBow());
        Permanent oldCreature = addCreatureReady(player1, new HillGiant());
        Permanent newCreature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        bow.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        resolveAllTriggers();

        assertThat(bow.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, newCreature, Keyword.REACH)).isTrue();
        assertThat(victim.getMarkedDamage()).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, oldCreature.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, newCreature.getId());
        resolveAllTriggers();

        assertThat(oldCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(newCreature.getMarkedDamage()).isZero();
    }

    @Test
    void missingDamageTargetDoesNotPreventAttachment() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        castBow(creature.getId(), victim.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hunter's Bow").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void missingAttachmentTargetPreventsDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        castBow(creature.getId(), victim.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hunter's Bow").getAttachedTo()).isNull();
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void destroyingBowInResponseDoesNotPreventCreatureDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        castBow(creature.getId(), victim.getId());
        harness.passBothPriorities();
        Permanent bow = findPermanent(player1, "Hunter's Bow");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bow.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hunter's Bow");
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void damageUsesCreaturesPowerWhenTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        castBow(creature.getId(), victim.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hunter's Bow").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(victim.getMarkedDamage()).isEqualTo(5);
    }

    private void castBow(java.util.UUID... targets) {
        harness.setHand(player1, List.of(new HuntersBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(targets), List.of());
    }

}
