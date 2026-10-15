package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryOfSunrise.class, Forest.class})
class EmissaryOfSunriseTest extends BaseCardTest {

    @Test
    @DisplayName("Explore with land on top puts land into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castEmissary();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Explore with land on top does not add +1/+1 counter")
    void exploreLandNoCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castEmissary();

        Permanent emissary = findEmissary();
        assertThat(emissary).isNotNull();
        assertThat(emissary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Explore with land on top does not prompt may ability")
    void exploreLandNoPrompt() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castEmissary();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore with non-land on top puts +1/+1 counter on creature")
    void exploreNonLandAddsCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new EmissaryOfSunrise());

        castEmissary();

        Permanent emissary = findEmissary();
        assertThat(emissary).isNotNull();
        assertThat(emissary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land on top prompts may ability")
    void exploreNonLandPromptsMayAbility() {
        gd.playerDecks.get(player1.getId()).addFirst(new EmissaryOfSunrise());

        castEmissary();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Explore non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Card creature = new EmissaryOfSunrise();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castEmissary();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore non-land — decline leaves card on top of library")
    void exploreNonLandDeclineLeavesOnTop() {
        Card creature = new EmissaryOfSunrise();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castEmissary();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore with empty library adds a counter without a choice")
    void exploreEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castEmissary();

        Permanent emissary = findEmissary();
        assertThat(emissary).isNotNull();
        assertThat(emissary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore still puts a land into hand after the source leaves")
    void exploreLandAfterSourceLeaves() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.castFromHand(player1, new EmissaryOfSunrise(), "{2}{W}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Explore still offers to graveyard a nonland after the source leaves")
    void exploreNonLandAfterSourceLeaves() {
        Card revealed = new EmissaryOfSunrise();
        harness.setLibrary(player1, List.of(revealed));
        harness.castFromHand(player1, new EmissaryOfSunrise(), "{2}{W}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Explore uses the creature's current controller after control changes")
    void exploreUsesCurrentController() {
        Card originalControllersLand = new Forest();
        Card currentControllersLand = new Forest();
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(currentControllersLand));
        harness.castFromHand(player1, new EmissaryOfSunrise(), "{2}{W}");
        harness.passBothPriorities();
        Permanent emissary = findEmissary();
        gd.playerBattlefields.get(player1.getId()).remove(emissary);
        gd.playerBattlefields.get(player2.getId()).add(emissary);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(currentControllersLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(originalControllersLand);
    }

    private void castEmissary() {
        harness.castFromHand(player1, new EmissaryOfSunrise(), "{2}{W}");

        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }

    private Permanent findEmissary() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Emissary of Sunrise"))
                .findFirst().orElse(null);
    }
}
