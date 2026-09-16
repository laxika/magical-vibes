package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicIntent.class, AncientSpider.class, ManaCylix.class})
class DiabolicIntentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Diabolic Intent sacrifices a creature as an additional cost")
    void castingSacrificesCreature() {
        Permanent sacrifice = addCreatureReady(player1, new AncientSpider());
        harness.setHand(player1, List.of(new DiabolicIntent()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ancient Spider");
        harness.assertInGraveyard(player1, "Ancient Spider");
    }

    @Test
    @DisplayName("Resolving Diabolic Intent searches a card into its controller's hand")
    void searchesCardIntoHand() {
        Permanent sacrifice = addCreatureReady(player1, new AncientSpider());
        ManaCylix searchedCard = new ManaCylix();
        harness.setLibrary(player1, List.of(searchedCard, new AncientSpider()));
        harness.setHand(player1, List.of(new DiabolicIntent()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Mana Cylix"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Ancient Spider");
        harness.assertInGraveyard(player1, "Diabolic Intent");
    }

    @Test
    @DisplayName("Diabolic Intent cannot sacrifice a noncreature permanent")
    void cannotSacrificeNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new ManaCylix());
        harness.setHand(player1, List.of(new DiabolicIntent()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Diabolic Intent cannot be cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new DiabolicIntent()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
