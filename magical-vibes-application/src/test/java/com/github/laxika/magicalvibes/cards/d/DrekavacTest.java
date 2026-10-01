package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.i.InfernalTutor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drekavac.class, GuardianOfTheGuildpact.class, InfernalTutor.class})
class DrekavacTest extends BaseCardTest {

    @Test
    @DisplayName("Only noncreature cards are offered for the discard")
    void onlyNoncreatureCardsCanBeDiscarded() {
        castDrekavac(List.of(new GuardianOfTheGuildpact(), new InfernalTutor()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Infernal Tutor");
        harness.assertInHand(player1, "Guardian of the Guildpact");
    }

    @Test
    @DisplayName("Any available noncreature card can satisfy the discard")
    void anyAvailableNoncreatureCardCanBeDiscarded() {
        castDrekavac(List.of(
                new GuardianOfTheGuildpact(), new InfernalTutor(), new InfernalTutor()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 2);

        harness.assertOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Infernal Tutor");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Infernal Tutor"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the discard sacrifices Drekavac")
    void decliningDiscardSacrificesDrekavac() {
        castDrekavac(List.of(new InfernalTutor()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInHand(player1, "Infernal Tutor");
    }

    @Test
    @DisplayName("No noncreature card automatically sacrifices Drekavac")
    void noNoncreatureCardAutomaticallySacrificesDrekavac() {
        castDrekavac(List.of(new GuardianOfTheGuildpact()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Drekavac");
        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInHand(player1, "Guardian of the Guildpact");
    }

    private void castDrekavac(List<Card> hand) {
        harness.castFromHand(player1, new Drekavac(), "{1}{B}");
        harness.setHand(player1, hand);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
