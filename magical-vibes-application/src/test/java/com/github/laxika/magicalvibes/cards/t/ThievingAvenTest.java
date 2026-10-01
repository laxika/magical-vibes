package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ThievingAven.class, Divination.class, Forest.class, GrizzlyBears.class, Island.class})
class ThievingAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage heists three random nonland cards from the target opponent")
    void combatDamageHeistsTargetOpponentsLibrary() {
        Permanent aven = addCreatureReady(player1, new ThievingAven());
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, first, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aven)));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.HeistCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HeistCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3)
                .allMatch(card -> !card.hasType(CardType.LAND));

        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Casting a spell you do not own puts a +1/+1 counter on Thieving Aven")
    void castingUnownedSpellAddsCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new ThievingAven());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination divination = new Divination();
        divination.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell you own does not put a +1/+1 counter on Thieving Aven")
    void castingOwnedSpellAddsNoCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new ThievingAven());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination divination = new Divination();
        divination.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
