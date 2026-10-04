package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshioksForerunner.class, AshiokSculptorOfFears.class})
class AshioksForerunnerTest extends BaseCardTest {

    @Test
    void acceptsOptionalSearchFromGraveyard() {
        Card ashiok = new AshiokSculptorOfFears();
        harness.setGraveyard(player1, List.of(ashiok));
        castForerunner();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ashiok, Sculptor of Fears");
        harness.assertNotInGraveyard(player1, "Ashiok, Sculptor of Fears");
    }

    @Test
    void acceptsOptionalSearchFromLibrary() {
        Card ashiok = new AshiokSculptorOfFears();
        harness.setLibrary(player1, List.of(ashiok));
        castForerunner();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Ashiok, Sculptor of Fears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    void maySearchCanBeDeclined() {
        Card ashiok = new AshiokSculptorOfFears();
        harness.setGraveyard(player1, List.of(ashiok));
        castForerunner();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ashiok, Sculptor of Fears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castForerunner() {
        harness.setHand(player1, List.of(new AshioksForerunner()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
