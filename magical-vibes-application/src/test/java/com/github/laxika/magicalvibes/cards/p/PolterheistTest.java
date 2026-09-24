package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Polterheist.class, Forest.class, GrizzlyBears.class, Shock.class})
class PolterheistTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay 3 life")
    void wardCountersUnpaidSpell() {
        Permanent polterheist = addCreatureReady(player1, new Polterheist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, polterheist.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Attacking heists three random nonland cards and lets the controller choose one")
    void attackingHeistsAndExilesChosenCard() {
        Permanent polterheist = addCreatureReady(player1, new Polterheist());
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, first, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(polterheist)));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.HeistCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HeistCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3).allMatch(card -> !card.hasType(CardType.LAND));

        UUID chosenId = choice.cards().getFirst().getId();
        harness.handleMultipleCardsChosen(player1, List.of(chosenId));

        assertThat(gd.findExiledCard(chosenId)).isNotNull();
        assertThat(gd.findExiledCard(chosenId).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions.get(chosenId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosenId);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(3)
                .contains(land)
                .allMatch(card -> !card.getId().equals(chosenId));
    }
}
