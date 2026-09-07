package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiendArtisan.class, Forest.class, GoldMyr.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class FiendArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void boostsForCreatureCardsInOwnGraveyard() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new Forest()));
        harness.setGraveyard(player2, List.of(new HillGiant()));

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices another creature and searches for a creature with mana value X or less")
    void sacrificesAndSearchesWithinX() {
        Permanent artisan = addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new LlanowarElves(), new GoldMyr(), new HillGiant(), new Forest()));

        harness.activateAbility(player1, 0, 2, null);

        assertThat(artisan.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Gold Myr");

        int goldMyrIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Gold Myr");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(goldMyrIndex));

        harness.assertOnBattlefield(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void sorcerySpeedRestriction() {
        addCreatureReady(player1, new FiendArtisan());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
