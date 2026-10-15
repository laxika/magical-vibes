package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.cards.p.PristineAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraftedWargear.class, SkyhunterProwler.class, PristineAngel.class})
class GraftedWargearTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void equipZeroAttachesWithoutMana() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reEquippingSacrificesPreviouslyEquippedCreature() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature1 = addCreatureReady(player1, new SkyhunterProwler());
        Permanent creature2 = addCreatureReady(player1, new SkyhunterProwler());
        wargear.setAttachedTo(creature1.getId());

        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        harness.assertInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void equippingFromUnattachedStateDoesNotSacrificeCreature() {
        harness.addToBattlefield(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyhunter Prowler");
        harness.assertNotInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void reEquippingToSameCreatureDoesNotSacrificeIt() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        wargear.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyhunter Prowler");
        harness.assertNotInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void removingEquipmentSacrificesEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wargear));

        harness.assertInGraveyard(player1, "Grafted Wargear");
        harness.assertOnBattlefield(player1, "Skyhunter Prowler");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void cannotSacrificeOpponentsCreatureWhenEquipmentLeaves() {
        Permanent creature = addCreatureReady(player2, new SkyhunterProwler());
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wargear));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Skyhunter Prowler");
        harness.assertNotInGraveyard(player2, "Skyhunter Prowler");
    }

    @Test
    void becomingIllegallyAttachedTriggersSacrifice() {
        Permanent angel = addCreatureReady(player1, new PristineAngel());
        angel.tap();
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(angel.getId());

        angel.untap();
        harness.runStateBasedActions();

        assertThat(wargear.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Pristine Angel");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pristine Angel");
        harness.assertOnBattlefield(player1, "Grafted Wargear");
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player2, new SkyhunterProwler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedEquipDoesNotUnattachOrSacrificeCurrentWearer() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent wearer = addCreatureReady(player1, new SkyhunterProwler());
        Permanent target = addCreatureReady(player1, new SkyhunterProwler());
        wargear.setAttachedTo(wearer.getId());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(wearer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wearer);
        assertThat(gd.stack).isEmpty();
    }
}
