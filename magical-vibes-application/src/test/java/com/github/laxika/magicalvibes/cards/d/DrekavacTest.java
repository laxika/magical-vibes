package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drekavac.class, Forest.class, GrizzlyBears.class})
class DrekavacTest extends BaseCardTest {

    @Test
    @DisplayName("Only noncreature cards are offered for the discard")
    void onlyNoncreatureCardsCanBeDiscarded() {
        castDrekavac(List.of(new GrizzlyBears(), new Forest()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the discard sacrifices Drekavac")
    void decliningDiscardSacrificesDrekavac() {
        castDrekavac(List.of(new Forest()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("No noncreature card automatically sacrifices Drekavac")
    void noNoncreatureCardAutomaticallySacrificesDrekavac() {
        castDrekavac(List.of(new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castDrekavac(List<Card> hand) {
        harness.setHand(player1, List.of(new Drekavac()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.setHand(player1, hand);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
