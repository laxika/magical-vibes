package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SokkaAndSuki.class, RecklessCohort.class, GrizzlyBears.class, LeoninScimitar.class})
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
        Permanent sokkaAndSuki = addReady(player1, new SokkaAndSuki());
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
        assertThat(ally.getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(equipment.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(sokkaAndSuki.getId()).isNotEqualTo(ally.getId());
    }

    @Test
    @DisplayName("A non-Ally creature does not trigger the attachment ability")
    void nonAllyDoesNotTrigger() {
        addReady(player1, new SokkaAndSuki());
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

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addSokkaAndSukiMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
