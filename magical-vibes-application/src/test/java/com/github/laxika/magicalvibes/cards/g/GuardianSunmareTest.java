package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianSunmareTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled offers a nonland permanent with mana value 3 or less")
    void attacksWhileSaddledSearchesForMatchingPermanent() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        sunmare.setSaddled(true);
        setLibrary(new BrightfieldGlider(), new Plains(), new GuardianSunmare(), new HolyDay());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Brightfield Glider");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Brightfield Glider");
    }

    @Test
    @DisplayName("Attacking while not saddled does not search")
    void doesNotSearchWhenNotSaddled() {
        addCreatureReady(player1, new GuardianSunmare());
        setLibrary(new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Brightfield Glider"));
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent sunmare = addCreatureReady(player1, new GuardianSunmare());
        setLibrary(new BrightfieldGlider());

        declareAttackers(player1, List.of(0));
        sunmare.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
