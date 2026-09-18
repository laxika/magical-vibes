package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SokkaSwordmaster.class, LeoninScimitar.class, GrizzlyBears.class})
class SokkaSwordmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Equipment spells cost less for each Ally controlled")
    void equipmentSpellsCostLessForEachAlly() {
        addCreatureReady(player1, new SokkaSwordmaster());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost reduction does not apply to non-Equipment spells")
    void costReductionDoesNotApplyToNonEquipmentSpells() {
        addCreatureReady(player1, new SokkaSwordmaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Beginning of combat attaches a targeted Equipment you control")
    void beginningOfCombatAttachesTargetedEquipment() {
        Permanent sokka = addCreatureReady(player1, new SokkaSwordmaster());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownEquipment.getId())
                .doesNotContain(opponentEquipment.getId());

        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.passBothPriorities();

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(sokka.getId());
    }
}
