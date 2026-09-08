package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoneforgeMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and offers an Equipment search that puts the chosen card into hand")
    void etbSearchesForEquipment() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Leonin Scimitar");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the enter-the-battlefield search leaves the library unchanged")
    void decliningEtbSearchDoesNothing() {
        harness.setHand(player1, List.of(new StoneforgeMystic()));
        harness.setLibrary(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Leonin Scimitar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability puts only an Equipment card from hand onto the battlefield")
    void activatedAbilityPutsEquipmentFromHandOntoBattlefield() {
        Permanent mystic = addCreatureReady(player1, new StoneforgeMystic());
        harness.setHand(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(mystic.isTapped()).isTrue();
    }
}
