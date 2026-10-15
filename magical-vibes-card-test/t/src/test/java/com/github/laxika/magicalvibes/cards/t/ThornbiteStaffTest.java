package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornbiteStaff.class, BoskBanneret.class, CruelEdict.class, GrizzlyBears.class})
class ThornbiteStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can pay {2} and tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature loses the granted ability when the Staff is removed")
    void creatureLosesAbilityWhenStaffRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(staff);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Equipped creature untaps whenever a creature dies")
    void untapsEquippedCreatureWhenCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruelEdictAtPlayer2();
        harness.passBothPriorities(); // resolve Cruel Edict → creature dies → untap trigger on stack
        harness.passBothPriorities(); // resolve untap trigger

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap trigger reaches the equipped creature, not the Staff itself")
    void untapsTheEquippedCreatureAndNotTheStaff() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());
        staff.tap();

        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruelEdictAtPlayer2();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unattached Staff does not trigger when a creature dies")
    void unattachedStaffDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        addStaffReady(player1); // left unattached

        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruelEdictAtPlayer2();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the may attaches the Staff to the Shaman that entered")
    void attachesToEnteringShamanOnAccept() {
        Permanent staff = addStaffReady(player1);

        harness.setHand(player1, List.of(new BoskBanneret()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature → Staff triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent shaman = shamanOnBattlefield(player1);
        assertThat(staff.getAttachedTo()).isEqualTo(shaman.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Shaman creature entering")
    void doesNotTriggerForNonShaman() {
        addStaffReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature — no trigger for a Bear

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip attaches the Staff for four mana")
    void equipAttachesForFourMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining attachment leaves the previous creature equipped")
    void decliningAttachmentKeepsPreviousCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new BoskBanneret()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The Staff controller may attach it to an opponent's entering Shaman")
    void attachesToOpponentsShaman() {
        Permanent staff = addStaffReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BoskBanneret()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        Permanent shaman = shamanOnBattlefield(player2);
        assertThat(staff.getAttachedTo()).isEqualTo(shaman.getId());

        shaman.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A pending untap ability untaps its original creature after the Staff moves")
    void pendingUntapDoesNotFollowStaff() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        original.tap();
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(original.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCruelEdictAtPlayer2();
        harness.passBothPriorities();

        Permanent shaman = harness.enterBattlefieldAndReturn(player1, new BoskBanneret());
        shaman.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(staff.getAttachedTo()).isEqualTo(shaman.getId());
        harness.passBothPriorities();

        assertThat(original.isTapped()).isFalse();
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted damage ability can damage a creature")
    void grantedAbilityDamagesCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }
    @Test
    @DisplayName("The equipped creature's controller controls the granted untap trigger")
    void creatureControllerControlsUntapTrigger() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        staff.setAttachedTo(creature.getId());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        victim.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
    }
    private void castCruelEdictAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private Permanent addStaffReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThornbiteStaff());
    }

    private Permanent shamanOnBattlefield(Player player) {
        return findPermanent(player, "Bosk Banneret");
    }
}
