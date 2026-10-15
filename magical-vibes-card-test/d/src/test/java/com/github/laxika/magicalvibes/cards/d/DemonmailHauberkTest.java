package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonmailHauberk.class, WalkingCorpse.class, AvacynsPilgrim.class})
class DemonmailHauberkTest extends BaseCardTest {

    @Test
    @DisplayName("Equip by sacrificing a creature attaches equipment to target creature")
    void equipBySacrificingCreature() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent sacrifice = addCreatureReady(player1, new AvacynsPilgrim());

        harness.activateAbility(player1, 0, null, creature.getId());
        // Choose the sacrifice as the creature to sacrifice
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(creature.getId());

        // The sacrifice is paid as a cost.
        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");

        // The equipped creature gets +4/+2.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip moves equipment from one creature to another")
    void equipMovesEquipmentBetweenCreatures() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent firstCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent firstSacrifice = addCreatureReady(player1, new AvacynsPilgrim());
        Permanent secondSacrifice = addCreatureReady(player1, new AvacynsPilgrim());

        // Equip to firstCreature by sacrificing firstSacrifice
        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Demonmail Hauberk").getAttachedTo())
                .isEqualTo(firstCreature.getId());

        // Add a second creature and equip to it by sacrificing secondSacrifice
        Permanent secondCreature = addCreatureReady(player1, new AvacynsPilgrim());

        int hauberkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Demonmail Hauberk"));
        harness.activateAbility(player1, hauberkIndex, null, secondCreature.getId());
        harness.handlePermanentChosen(player1, secondSacrifice.getId());
        harness.passBothPriorities();

        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(secondCreature.getId());

        // Second creature gets +4/+2
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);

        // First creature no longer has bonus
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice the currently equipped creature to re-equip to another")
    void canSacrificeEquippedCreatureToReEquip() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent firstCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent sacrifice = addCreatureReady(player1, new AvacynsPilgrim());
        Permanent secondCreature = addCreatureReady(player1, new AvacynsPilgrim());

        // Equip to firstCreature by sacrificing sacrifice
        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        // Now equip to secondCreature by sacrificing firstCreature (the equipped creature)
        int hauberkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Demonmail Hauberk"));
        harness.activateAbility(player1, hauberkIndex, null, secondCreature.getId());
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.passBothPriorities();

        // The previously equipped creature is sacrificed.
        harness.assertNotOnBattlefield(player1, "Walking Corpse");

        // Equipment should be attached to secondCreature
        Permanent hauberk = findPermanent(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Sacrifice is paid before the equip ability resolves")
    void sacrificeIsPaidAtActivation() {
        Permanent hauberk = harness.addToBattlefieldAndReturn(player1, new DemonmailHauberk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Walking Corpse");
        assertThat(hauberk.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(hauberk.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("The equip target itself may be sacrificed to pay the cost")
    void canSacrificeTheTargetItself() {
        Permanent hauberk = harness.addToBattlefieldAndReturn(player1, new DemonmailHauberk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new AvacynsPilgrim());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Walking Corpse");
        assertThat(hauberk.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent hauberk = harness.addToBattlefieldAndReturn(player1, new DemonmailHauberk());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(hauberk.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new DemonmailHauberk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment is not a valid creature sacrifice")
    void cannotSacrificeTheNoncreatureEquipment() {
        Permanent hauberk = harness.addToBattlefieldAndReturn(player1, new DemonmailHauberk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new AvacynsPilgrim());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hauberk.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Demonmail Hauberk");
        assertThat(hauberk.getAttachedTo()).isNull();
    }

}
