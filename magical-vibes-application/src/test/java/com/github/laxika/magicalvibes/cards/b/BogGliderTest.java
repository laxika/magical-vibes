package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.a.AgentOfShauku;
import com.github.laxika.magicalvibes.cards.d.DeathCharmer;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

    @Test
    void mayFailToFindEvenWhenEligibleMercenaryIsPresent() {
        addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLibrary(player1, List.of(new AgentOfShauku()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Rhystic Cave");
        harness.assertNotOnBattlefield(player1, "Agent of Shauku");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Agent of Shauku");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BogGlider());
        harness.addToBattlefield(player1, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Rhystic Cave");
        harness.assertNotInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        glider.tap();
        harness.addToBattlefield(player1, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Rhystic Cave");
        harness.assertNotInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    void cannotSacrificeOpponentsLand() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glider.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Rhystic Cave");
        harness.assertNotInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    void resolvesWithEmptyLibraryAfterPayingCosts() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glider.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Rhystic Cave");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new BogGlider());
        addCreatureReady(player2, new AgentOfShauku());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockBogGlider() {
        Permanent attacker = addCreatureReady(player1, new BogGlider());
        Permanent blocker = addCreatureReady(player2, new BogGlider());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
