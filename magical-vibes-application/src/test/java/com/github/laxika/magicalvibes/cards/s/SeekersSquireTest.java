package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SeekersSquire.class, Forest.class, LightningStrike.class})
class SeekersSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Explore with land on top puts land into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castSquire();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Explore with land on top does not add +1/+1 counter")
    void exploreLandNoCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castSquire();

        Permanent squire = findSquire();
        assertThat(squire).isNotNull();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Explore with land on top does not prompt may ability")
    void exploreLandNoPrompt() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castSquire();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore with non-land on top puts +1/+1 counter on creature")
    void exploreNonLandAddsCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new SeekersSquire());

        castSquire();

        Permanent squire = findSquire();
        assertThat(squire).isNotNull();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land on top prompts may ability")
    void exploreNonLandPromptsMayAbility() {
        gd.playerDecks.get(player1.getId()).addFirst(new SeekersSquire());

        castSquire();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Explore non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Card creature = new SeekersSquire();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castSquire();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore non-land — decline leaves card on top of library")
    void exploreNonLandDeclineLeavesOnTop() {
        Card creature = new SeekersSquire();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castSquire();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore with empty library adds a counter without prompting")
    void exploreEmptyLibraryAddsCounter() {
        harness.setLibrary(player1, List.of());

        castSquire();

        Permanent squire = findSquire();
        assertThat(squire).isNotNull();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore still puts a revealed land into hand after the Squire dies")
    void exploreLandAfterSquireDies() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        resolveExploreAfterSquireDies();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Explore still offers to put a nonland into the graveyard after the Squire dies")
    void exploreNonLandAfterSquireDies() {
        Card revealed = new SeekersSquire();
        harness.setLibrary(player1, List.of(revealed));

        resolveExploreAfterSquireDies();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveExploreAfterSquireDies() {
        harness.setHand(player1, List.of(new SeekersSquire(), new LightningStrike()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Seekers' Squire"));
        harness.assertNotOnBattlefield(player1, "Seekers' Squire");
        harness.passBothPriorities();
    }

    private void castSquire() {
        harness.setHand(player1, List.of(new SeekersSquire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }

    private Permanent findSquire() {
        return harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Seekers' Squire"));
    }
}
