package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkIntimationsTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices, discards, and the controller returns a creature then draws")
    void resolvesAllSpellEffects() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Shock())));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new DarkIntimations()));
        addDarkIntimationsMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A Bolas planeswalker enters with an additional loyalty counter and exiles Dark Intimations")
    void triggersForBolasPlaneswalkerSpells() {
        DarkIntimations darkIntimations = new DarkIntimations();
        harness.setGraveyard(player1, List.of(darkIntimations));
        harness.setHand(player1, List.of(new NicolBolasGodPharaoh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        Permanent bolas = findPermanent(player1, "Nicol Bolas, God-Pharaoh");
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(darkIntimations.getId()));
        harness.assertNotInGraveyard(player1, "Dark Intimations");
    }

    @Test
    @DisplayName("The graveyard ability does not trigger for a non-Bolas planeswalker")
    void doesNotTriggerForNonBolasPlaneswalkerSpells() {
        harness.setGraveyard(player1, List.of(new DarkIntimations()));
        harness.setHand(player1, List.of(new LilianaVess()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dark Intimations");
    }

    private void addDarkIntimationsMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
