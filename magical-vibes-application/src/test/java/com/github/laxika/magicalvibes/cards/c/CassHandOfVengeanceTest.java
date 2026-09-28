package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PowerFist;
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
        PowerFist.class, Shock.class})
class CassHandOfVengeanceTest extends BaseCardTest {

    @Test
    void returnsChosenAurasAndReattachesChosenEquipmentWhenAnotherCreatureDies() {
        addCreatureReady(player1, new CassHandOfVengeance());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        Permanent auraPermanent = new Permanent(aura);
        auraPermanent.setAttachedTo(dyingCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPermanent);
        PowerFist powerFist = new PowerFist();
        Permanent equipment = new Permanent(powerFist);
        equipment.setAttachedTo(dyingCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(equipment);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(dyingCreature);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice attachmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(attachmentChoice.validIds()).containsExactly(equipment.getId());
        assertThat(attachmentChoice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId(), equipment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(aura.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void triggersWhenCassDiesAndCanAttachItsEquipmentToTheChosenCreature() {
        Permanent cass = addCreatureReady(player1, new CassHandOfVengeance());
        PowerFist powerFist = new PowerFist();
        Permanent equipment = new Permanent(powerFist);
        equipment.setAttachedTo(cass.getId());
        gd.playerBattlefields.get(player1.getId()).add(equipment);
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

    private void destroyWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyWithMurder(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
