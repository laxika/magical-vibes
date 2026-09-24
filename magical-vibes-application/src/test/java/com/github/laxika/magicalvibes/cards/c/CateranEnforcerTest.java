package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CateranEnforcer.class, CateranKidnappers.class, CateranSlaver.class, CacklingWitch.class})
class CateranEnforcerTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostFour() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setLibrary(player1, List.of(new CateranKidnappers(), new CacklingWitch(), new CateranSlaver()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cateran Enforcer").isTapped()).isTrue();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Kidnappers");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Kidnappers");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cackling Witch", "Cateran Slaver");
    }

    @Test
    void resolvesWithoutInteractionWhenNoMercenaryPermanentWithManaValueAtMostFourExists() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new CacklingWitch(), new CateranSlaver()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cackling Witch", "Cateran Slaver");
    }
}
