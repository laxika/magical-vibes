package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.t.ThoughtErasure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepingChill.class, Millstone.class, ThoughtErasure.class})
class CreepingChillTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Creeping Chill damages each opponent and gains life")
    void castingDamagesEachOpponentAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CreepingChill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Creeping Chill");
    }

    @Test
    @DisplayName("When milled, accepting Creeping Chill's trigger exiles it, damages each opponent, and gains life")
    void acceptingSelfMillTriggerExilesItAndResolvesEffect() {
        Card chill = setUpMill();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(chill.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(chill.getId()));
    }

    @Test
    @DisplayName("Declining Creeping Chill's mill trigger leaves it in the graveyard")
    void decliningSelfMillTriggerLeavesItInGraveyard() {
        Card chill = setUpMill();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chill.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(chill.getId()));
    }

    @Test
    @DisplayName("Putting Creeping Chill into the graveyard with surveil triggers its optional exile ability")
    void surveillingIntoGraveyardTriggersAbility() {
        Card chill = new CreepingChill();
        harness.setLibrary(player1, List.of(chill));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Creeping Chill");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertNotInGraveyard(player1, "Creeping Chill");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(chill.getId()));
    }

    @Test
    @DisplayName("Keeping Creeping Chill on top while surveilling does not trigger its ability")
    void keepingOnTopWhileSurveillingDoesNotTrigger() {
        Card chill = new CreepingChill();
        harness.setLibrary(player1, List.of(chill));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chill);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Creeping Chill");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Card setUpMill() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new Millstone());

        Card chill = new CreepingChill();
        harness.setLibrary(player1, List.of(chill));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        return chill;
    }
}
