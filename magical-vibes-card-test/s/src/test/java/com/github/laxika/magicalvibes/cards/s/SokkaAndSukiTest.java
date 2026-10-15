package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkaAndSuki.class, RecklessCohort.class, GrizzlyBears.class, LeoninScimitar.class, Conspiracy.class})
class SokkaAndSukiTest extends BaseCardTest {

    @Test
    @DisplayName("Sokka and Suki attaches a target Equipment to itself when it enters")
    void attachesEquipmentToItselfWhenEntering() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new SokkaAndSuki()));
        addSokkaAndSukiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        Permanent sokkaAndSuki = findPermanent(player1, "Sokka and Suki");
        assertThat(equipment.getAttachedTo()).isEqualTo(sokkaAndSuki.getId());
    }

    @Test
    @DisplayName("Sokka and Suki attaches a target Equipment to another entering Ally")
    void attachesEquipmentToAnotherEnteringAlly() {
        Permanent sokkaAndSuki = harness.addToBattlefieldAndReturn(player1, new SokkaAndSuki());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new RecklessCohort()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Reckless Cohort");
        assertThat(equipment.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(sokkaAndSuki.getId()).isNotEqualTo(ally.getId());
    }

    @Test
    @DisplayName("A non-Ally creature does not trigger the attachment ability")
    void nonAllyDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new SokkaAndSuki());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An Equipment entering under your control creates a 1/1 white Ally token")
    void equipmentEntryCreatesAllyToken() {
        harness.addToBattlefield(player1, new SokkaAndSuki());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getPower()).isEqualTo(1);
        assertThat(ally.getCard().getToughness()).isEqualTo(1);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("The controller may choose no Equipment even when one is available")
    void mayDeclineAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new SokkaAndSuki()));
        addSokkaAndSukiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering with no Equipment resolves without requiring a target")
    void entersWithoutEquipment() {
        harness.setHand(player1, List.of(new SokkaAndSuki()));
        addSokkaAndSukiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sokka and Suki")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled Equipment cannot be selected")
    void excludesOpponentEquipment() {
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new SokkaAndSuki()));
        addSokkaAndSukiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(ownEquipment.getId()).doesNotContain(opponentEquipment.getId());
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.passBothPriorities();

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Sokka and Suki").getId());
        assertThat(opponentEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Opponent Ally and Equipment entries do not trigger either ability")
    void opponentEntriesDoNotTrigger() {
        harness.addToBattlefield(player1, new SokkaAndSuki());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.enterBattlefieldAndReturn(player2, new RecklessCohort());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The Ally token from an Equipment entry can receive that Equipment")
    void createdAllyTriggersAttachment() {
        harness.addToBattlefield(player1, new SokkaAndSuki());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        Permanent equipment = findPermanent(player1, "Leonin Scimitar");
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Sokka and Suki triggers for itself even when its Ally type is replaced")
    void selfEntryDoesNotRequireAllyType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new SokkaAndSuki()));
        addSokkaAndSukiMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Sokka and Suki").getId());
    }
    private void addSokkaAndSukiMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
