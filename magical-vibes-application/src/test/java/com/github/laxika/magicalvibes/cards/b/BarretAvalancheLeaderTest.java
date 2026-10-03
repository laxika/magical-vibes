package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({BarretAvalancheLeader.class, LeoninScimitar.class})
class BarretAvalancheLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Barret creates a 2/2 Rebel when an Equipment enters under your control")
    void createsRebelWhenEquipmentEnters() {
        addCreatureReady(player1, new BarretAvalancheLeader());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
    }

    @Test
    @DisplayName("Barret attaches up to one target Equipment to a target Rebel at combat")
    void attachesEquipmentToTargetRebelAtBeginningOfCombat() {
        addCreatureReady(player1, new BarretAvalancheLeader());
        Permanent rebel = addCreatureReady(player1, rebelToken());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(rebel.getId());
        harness.handlePermanentChosen(player1, rebel.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(rebel.getId());
    }

    @Test
    void opposingEquipmentDoesNotCreateRebel() {
        addCreatureReady(player1, new BarretAvalancheLeader());

        harness.enterBattlefieldAndReturn(player2, new LeoninScimitar());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void eachEquipmentEntryCreatesAnotherRebel() {
        addCreatureReady(player1, new BarretAvalancheLeader());

        harness.enterBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void canAttachEquipmentToBarretHimselfAndExcludeOpposingPermanents() {
        Permanent barret = addCreatureReady(player1, new BarretAvalancheLeader());
        Permanent opposingBarret = addCreatureReady(player2, new BarretAvalancheLeader());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(equipment.getId()).doesNotContain(opposingEquipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(barret.getId()).doesNotContain(opposingBarret.getId());
        harness.handlePermanentChosen(player1, barret.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(barret.getId());
        assertThat(opposingEquipment.getAttachedTo()).isNull();
    }

    @Test
    void canDeclineEquipmentWhileStillChoosingRebel() {
        Permanent barret = addCreatureReady(player1, new BarretAvalancheLeader());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(barret.getId());
        harness.handlePermanentChosen(player1, barret.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stillTargetsRebelWhenNoEquipmentIsAvailable() {
        Permanent barret = addCreatureReady(player1, new BarretAvalancheLeader());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(barret.getId());
        harness.handlePermanentChosen(player1, barret.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(barret);
    }

    @Test
    void doesNotTriggerAtOpponentsBeginningOfCombat() {
        addCreatureReady(player1, new BarretAvalancheLeader());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void doesNotAttachWhenTargetRebelLeavesBeforeResolution() {
        Permanent barret = addCreatureReady(player1, new BarretAvalancheLeader());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, barret.getId());
        gd.playerBattlefields.get(player1.getId()).remove(barret);
        gd.playerGraveyards.get(player1.getId()).add(barret.getCard());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Card rebelToken() {
        Card token = new Card();
        token.setName("Rebel");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.RED);
        token.setSubtypes(List.of(CardSubtype.REBEL));
        token.setPower(2);
        token.setToughness(2);
        token.setToken(true);
        return token;
    }
}
