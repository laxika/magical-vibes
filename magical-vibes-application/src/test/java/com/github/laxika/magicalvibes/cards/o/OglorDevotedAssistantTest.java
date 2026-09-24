package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OglorDevotedAssistant.class, Forest.class, GrizzlyBears.class, MindRot.class, Zombify.class})
class OglorDevotedAssistantTest extends BaseCardTest {

    @Test
    void upkeepPutsOneOfTopTwoCardsIntoGraveyardAndTheOtherBackOnTop() {
        harness.addToBattlefield(player1, new OglorDevotedAssistant());
        Card graveyardCard = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(graveyardCard, libraryCard));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void creatureDiscardedFromHandGainsAbilityThatCreatesTappedZombieWhenItLeavesGraveyard() {
        harness.addToBattlefield(player1, new OglorDevotedAssistant());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new MindRot(), creature, new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.perpetualTriggeredAbilities).containsKey(creature.getId());

        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo("Zombie");
                });
    }
}
