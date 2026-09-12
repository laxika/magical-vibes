package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.a.AgentOfShauku;
import com.github.laxika.magicalvibes.cards.d.DeathCharmer;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogGlider.class, RhysticCave.class, AgentOfShauku.class, DeathCharmer.class, Abolish.class})
class BogGliderTest extends BaseCardTest {

    @Test
    void sacrificesALandToPutEligibleMercenaryOntoBattlefield() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLibrary(player1, List.of(new AgentOfShauku(), new DeathCharmer(), new Abolish()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(glider.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Rhystic Cave");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Agent of Shauku");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Agent of Shauku");
        harness.assertNotOnBattlefield(player1, "Death Charmer");
        harness.assertNotOnBattlefield(player1, "Abolish");
    }

    @Test
    void cannotActivateWithoutALandToSacrifice() {
        Permanent glider = addCreatureReady(player1, new BogGlider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glider.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutInteractionWhenLibraryHasNoEligibleMercenary() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLibrary(player1, List.of(new DeathCharmer(), new Abolish()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glider.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Death Charmer");
        harness.assertNotOnBattlefield(player1, "Abolish");
    }
}
