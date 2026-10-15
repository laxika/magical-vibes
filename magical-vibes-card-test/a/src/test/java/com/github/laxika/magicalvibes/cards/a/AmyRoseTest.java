package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmyRose.class, GrizzlyBears.class, LeoninScimitar.class, Unsummon.class})
class AmyRoseTest extends BaseCardTest {

    @Test
    void attachesEquipmentAndBoostsAnotherAttackerByAmysPower() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(equipment.getId())
                .doesNotContain(amy.getId(), otherAttacker.getId());
        harness.handlePermanentChosen(player1, equipment.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(otherAttacker.getId())
                .doesNotContain(amy.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(amy.getId());
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(6);
    }

    @Test
    void canDeclineEquipmentAndStillBoostAnotherAttacker() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(otherAttacker.getId())
                .doesNotContain(amy.getId(), nonattacker.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
    }

    @Test
    void canAttachEquipmentWithoutChoosingAnotherAttacker() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(amy.getId());
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    void canDeclineBothTargets() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    void movesOpponentsEquipmentFromItsPreviousWearerWithoutChangingControl() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent previousWearer = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(previousWearer.getId());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(amy.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(gqs.getEffectivePower(gd, previousWearer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(6);
    }

    @Test
    void stillAttachesEquipmentWhenOtherAttackerLeavesBeforeResolution() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.castAndResolveInstant(player1, 0, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        assertThat(equipment.getAttachedTo()).isEqualTo(amy.getId());
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(4);
    }

    @Test
    void usesAmysLastKnownPowerWhenSheLeavesBeforeResolution() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.castAndResolveInstant(player1, 0, amy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amy);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(5);
    }
}
