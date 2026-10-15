package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
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

@CardUsed({SunriseSeeker.class, Forest.class, QueensBaySoldier.class, PerilousVoyage.class})
class SunriseSeekerTest extends BaseCardTest {


    @Test
    @DisplayName("Explore with land on top puts land into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castSeeker();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Explore with land on top does not add +1/+1 counter")
    void exploreLandNoCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castSeeker();

        Permanent seeker = findSeeker();
        assertThat(seeker).isNotNull();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Explore with land on top does not prompt may ability")
    void exploreLandNoPrompt() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castSeeker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }


    @Test
    @DisplayName("Explore with non-land on top puts +1/+1 counter on creature")
    void exploreNonLandAddsCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new QueensBaySoldier());

        castSeeker();

        Permanent seeker = findSeeker();
        assertThat(seeker).isNotNull();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land on top prompts may ability")
    void exploreNonLandPromptsMayAbility() {
        gd.playerDecks.get(player1.getId()).addFirst(new QueensBaySoldier());

        castSeeker();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Explore non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Card creature = new QueensBaySoldier();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castSeeker();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore non-land — decline leaves card on top of library")
    void exploreNonLandDeclineLeavesOnTop() {
        Card creature = new QueensBaySoldier();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castSeeker();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }


    @Test
    @DisplayName("Explore with empty library adds a counter without prompting")
    void exploreEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castSeeker();

        Permanent seeker = findSeeker();
        assertThat(seeker).isNotNull();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore still puts a land into hand after the creature leaves the battlefield")
    void exploreLandAfterSeekerLeaves() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castSeekerAndBounceBeforeExplore();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sunrise Seeker");
        harness.assertInHand(player1, "Sunrise Seeker");
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore still allows a nonland to go to the graveyard after the creature leaves")
    void exploreNonLandAfterSeekerLeaves() {
        Card revealed = new QueensBaySoldier();
        harness.setLibrary(player1, List.of(revealed));

        castSeekerAndBounceBeforeExplore();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sunrise Seeker");
        harness.assertInHand(player1, "Sunrise Seeker");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castSeekerAndBounceBeforeExplore() {
        harness.setHand(player1, List.of(new SunriseSeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Sunrise Seeker"));
    }


    private void castSeeker() {
        harness.setHand(player1, List.of(new SunriseSeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }

    private Permanent findSeeker() {
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Sunrise Seeker"));
    }
}
