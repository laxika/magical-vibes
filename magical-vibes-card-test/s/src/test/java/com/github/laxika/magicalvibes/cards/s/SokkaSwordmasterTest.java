package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriorGuard;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
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

@CardUsed({SokkaSwordmaster.class, LeoninScimitar.class, GrizzlyBears.class,
        KyoshiWarriorGuard.class, LoxodonWarhammer.class})
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

    @Test
    void countsOtherAlliesButNotOpposingAlliesOrNonAllies() {
        addCreatureReady(player1, new SokkaSwordmaster());
        addCreatureReady(player1, new KyoshiWarriorGuard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KyoshiWarriorGuard());
        harness.setHand(player1, List.of(new LoxodonWarhammer()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opposingSokkaDoesNotReduceYourEquipmentCost() {
        addCreatureReady(player2, new SokkaSwordmaster());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void mayChooseNoEquipmentEvenWhenOneIsAvailable() {
        addCreatureReady(player1, new SokkaSwordmaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        beginCombat(player1);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noEquipmentDoesNotRequireTargetSelection() {
        addCreatureReady(player1, new SokkaSwordmaster());
        beginCombat(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new SokkaSwordmaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        beginCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void movesEquipmentFromAnotherCreatureWithoutPayingEquipCost() {
        Permanent sokka = addCreatureReady(player1, new SokkaSwordmaster());
        Permanent bearer = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(bearer.getId());
        beginCombat(player1);

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(sokka.getId());
        assertThat(gqs.getEffectivePower(gd, sokka)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bearer)).isEqualTo(2);
    }

    @Test
    void missingSokkaCannotReceiveEquipmentWhenTriggerResolves() {
        Permanent sokka = addCreatureReady(player1, new SokkaSwordmaster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        beginCombat(player1);
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sokka);
        harness.setGraveyard(player1, List.of(sokka.getCard()));

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
