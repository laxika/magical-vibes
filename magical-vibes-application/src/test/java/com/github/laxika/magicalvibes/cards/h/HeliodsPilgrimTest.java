package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeliodsPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers Aura cards from the library")
    void acceptingEtbAbilityOffersAuras() {
        Card aura = new Pacifism();
        setupAndCast(List.of(aura, new GrizzlyBears()));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(aura);
    }

    @Test
    @DisplayName("Choosing an Aura card puts it into hand")
    void choosingAuraPutsItIntoHand() {
        Card aura = new Pacifism();
        setupAndCast(List.of(aura));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the library search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast(List.of(new Pacifism()));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new HeliodsPilgrim()));
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(library);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
