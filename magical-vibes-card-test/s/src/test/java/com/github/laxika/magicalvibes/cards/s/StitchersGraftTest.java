package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.r.RuneboundWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StitchersGraft.class, RuneboundWolf.class, Abrade.class, ImprisonedInTheMoon.class, MagneticTheft.class})
class StitchersGraftTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking with the equipped creature keeps it from untapping next untap step")
    void attackTriggerLocksEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        assertThat(graft.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("An unequipped creature attacking is not locked")
    void attackWithoutEquipmentDoesNotLock() {
        Permanent creature = addCreatureReady(player1, new RuneboundWolf());
        addGraftReady(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Re-equipping sacrifices the previously equipped creature")
    void reEquipSacrificesPreviousCreature() {
        Permanent graft = addGraftReady(player1);
        Permanent creature1 = addCreatureReady(player1, new RuneboundWolf());
        Permanent creature2 = addCreatureReady(player1, new RuneboundWolf());
        graft.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        resolveAllTriggers();

        assertThat(graft.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        harness.assertInGraveyard(player1, "Runebound Wolf");
    }

    @Test
    @DisplayName("Equipping from unattached state sacrifices nothing")
    void equippingFromUnattachedDoesNotSacrifice() {
        addGraftReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneboundWolf());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Runebound Wolf");
    }


    @Test
    void reEquippingQueuesSacrificeBeforeTheFormerBearerDies() {
        Permanent graft = addGraftReady(player1);
        Permanent formerBearer = addCreatureReady(player1, new RuneboundWolf());
        Permanent newBearer = addCreatureReady(player1, new RuneboundWolf());
        graft.setAttachedTo(formerBearer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, newBearer.getId());
        harness.passBothPriorities();

        assertThat(graft.getAttachedTo()).isEqualTo(newBearer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(formerBearer);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(formerBearer);
    }

    @Test
    void destroyingGraftQueuesSacrificeInsteadOfSacrificingDuringTheSpell() {
        Permanent bearer = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(bearer.getId());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 1, graft.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stitcher's Graft");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bearer);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Runebound Wolf");
    }

    @Test
    void graftControllerCannotSacrificeAnOpponentsBearer() {
        Permanent bearer = addCreatureReady(player2, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(bearer.getId());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 1, graft.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bearer);
        harness.assertNotInGraveyard(player2, "Runebound Wolf");
    }

    @Test
    void becomingALandUnattachesGraftAndTriggersSacrifice() {
        Permanent bearer = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(bearer.getId());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bearer.getId());
        harness.passBothPriorities();

        assertThat(graft.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bearer);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Runebound Wolf");
        harness.assertInGraveyard(player1, "Imprisoned in the Moon");
    }

    @Test
    void attackRestrictionDoesNotMoveToTheNewBearer() {
        Permanent attacker = addCreatureReady(player1, new RuneboundWolf());
        Permanent newBearer = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0));
        harness.castInstant(player1, 0, List.of(graft.getId(), newBearer.getId()));
        resolveAllTriggers();

        assertThat(graft.getAttachedTo()).isEqualTo(newBearer.getId());
        assertThat(newBearer.getSkipUntapCount()).isZero();
        assertThat(newBearer.isTapped()).isFalse();
    }

    @Test
    void attackRestrictionExpiresAfterOneControllerUntapStep() {
        Permanent bearer = addCreatureReady(player1, new RuneboundWolf());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(bearer.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(bearer.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(bearer.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(bearer.isTapped()).isFalse();
    }

    @Test
    void twoGraftsOnlyPreventOneUntapStep() {
        Permanent bearer = addCreatureReady(player1, new RuneboundWolf());
        addGraftReady(player1).setAttachedTo(bearer.getId());
        addGraftReady(player1).setAttachedTo(bearer.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bearer)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bearer)).isEqualTo(8);
        harness.performUntapStep(player1);
        assertThat(bearer.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(bearer.isTapped()).isFalse();
    }

    @Test
    void equippingTheSameCreatureDoesNotUnattachOrSacrificeIt() {
        Permanent graft = addGraftReady(player1);
        Permanent bearer = addCreatureReady(player1, new RuneboundWolf());
        graft.setAttachedTo(bearer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bearer.getId());
        resolveAllTriggers();

        assertThat(graft.getAttachedTo()).isEqualTo(bearer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bearer);
        harness.assertNotInGraveyard(player1, "Runebound Wolf");
    }

    private Permanent addGraftReady(Player player) {
        return addCreatureReady(player, new StitchersGraft());
    }
}
