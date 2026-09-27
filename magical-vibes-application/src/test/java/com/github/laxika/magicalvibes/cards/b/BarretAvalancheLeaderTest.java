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
        assertThat(choice.validIds()).containsExactly(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(rebel.getId());
        harness.handlePermanentChosen(player1, rebel.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(rebel.getId());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
