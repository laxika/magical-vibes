package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.m.MirranCrusader;
import com.github.laxika.magicalvibes.cards.p.PowerFist;
import com.github.laxika.magicalvibes.cards.r.Reprobation;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CassHandOfVengeance.class, GrizzlyBears.class, HolyStrength.class, Murder.class,
        PowerFist.class, Shock.class, MirranCrusader.class, Reprobation.class})
class CassHandOfVengeanceTest extends BaseCardTest {

    @Test
    void returnsChosenAurasAndReattachesChosenEquipmentWhenAnotherCreatureDies() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(player1, aura);
        auraPermanent.setAttachedTo(dyingCreature.getId());
        PowerFist powerFist = new PowerFist();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, powerFist);
        equipment.setAttachedTo(dyingCreature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(dyingCreature);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice attachmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(attachmentChoice.validIds()).isEmpty();
        assertThat(attachmentChoice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(aura.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void triggersWhenCassDiesAndCanAttachItsEquipmentToTheChosenCreature() {
        Permanent cass = addCreatureReady(player1, new CassHandOfVengeance());
        PowerFist powerFist = new PowerFist();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, powerFist);
        equipment.setAttachedTo(cass.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(cass);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void doesNotTriggerForAnUnenchantedAndUnequippedDeath() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        destroyWithShock(dyingCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineAllAttachments() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(player1, aura);
        auraPermanent.setAttachedTo(dyingCreature.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        equipment.setAttachedTo(dyingCreature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(dyingCreature);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsAnAuraWithoutEquipmentAndLeavesUnrelatedAurasInTheGraveyard() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(player1, aura);
        auraPermanent.setAttachedTo(dyingCreature.getId());
        HolyStrength unrelatedAura = new HolyStrength();
        gd.playerGraveyards.get(player1.getId()).add(unrelatedAura);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(dyingCreature);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(aura.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unrelatedAura).doesNotContain(aura);
    }

    @Test
    void doesNotTriggerForAnEquippedOpponentCreature() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PowerFist());
        equipment.setAttachedTo(dyingCreature.getId());

        destroyWithMurder(dyingCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void canChooseEquipmentMadeLegalByTheReturnedAura() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Reprobation aura = new Reprobation();
        Permanent auraPermanent = harness.addToBattlefieldAndReturn(player1, aura);
        auraPermanent.setAttachedTo(dyingCreature.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        equipment.setAttachedTo(dyingCreature.getId());
        Permanent target = addCreatureReady(player2, new MirranCrusader());

        destroyWithMurder(dyingCreature);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validCardIds()).contains(aura.getId());
        if (choice.validIds().contains(equipment.getId())) {
            harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId(), equipment.getId()));
        } else {
            harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));
            PendingInteraction.MultiPermanentChoice equipmentChoice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
            assertThat(equipmentChoice).isNotNull();
            assertThat(equipmentChoice.validIds()).contains(equipment.getId());
            harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(aura.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    private void destroyWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void destroyWithMurder(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
