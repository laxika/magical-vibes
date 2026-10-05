package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({IxallisDiviner.class, Forest.class, LightningStrike.class, RayOfCommand.class})
class IxallisDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Explore with land on top puts land into hand")
    void exploreLandGoesToHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castDiviner();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Explore with land on top does not add +1/+1 counter")
    void exploreLandNoCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castDiviner();

        Permanent diviner = findDiviner();
        assertThat(diviner).isNotNull();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Explore with land on top does not prompt may ability")
    void exploreLandNoPrompt() {
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castDiviner();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore with non-land on top puts +1/+1 counter on creature")
    void exploreNonLandAddsCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new IxallisDiviner());

        castDiviner();

        Permanent diviner = findDiviner();
        assertThat(diviner).isNotNull();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land on top prompts may ability")
    void exploreNonLandPromptsMayAbility() {
        gd.playerDecks.get(player1.getId()).addFirst(new IxallisDiviner());

        castDiviner();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Explore non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Card creature = new IxallisDiviner();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castDiviner();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore non-land — decline leaves card on top of library")
    void exploreNonLandDeclineLeavesOnTop() {
        Card creature = new IxallisDiviner();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castDiviner();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Explore with empty library adds a +1/+1 counter without prompting")
    void exploreEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        castDiviner();

        Permanent diviner = findDiviner();
        assertThat(diviner).isNotNull();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Explore uses the creature's current controller's library after control changes")
    void exploreLandAfterControlChanges() {
        Card originalControllersCard = new IxallisDiviner();
        Card currentControllersLand = new Forest();
        harness.setLibrary(player1, List.of(originalControllersCard));
        harness.setLibrary(player2, List.of(currentControllersLand));

        Permanent diviner = stealDivinerBeforeExplore();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(currentControllersLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersCard);
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The current controller chooses whether to graveyard the explored nonland")
    void exploreNonlandAfterControlChanges() {
        Card originalControllersLand = new Forest();
        Card currentControllersCard = new IxallisDiviner();
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(currentControllersCard));

        Permanent diviner = stealDivinerBeforeExplore();
        harness.passBothPriorities();

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(currentControllersCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersLand);
    }

    @Test
    @DisplayName("Explore still puts a land into hand after the Diviner dies")
    void exploreAfterDivinerDies() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.castFromHand(player1, new IxallisDiviner(), "{1}{G}");
        harness.passBothPriorities();
        Permanent diviner = findDiviner();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, diviner.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(diviner);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(diviner.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent stealDivinerBeforeExplore() {
        harness.castFromHand(player1, new IxallisDiviner(), "{1}{G}");
        harness.passBothPriorities();
        Permanent diviner = findDiviner();
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, diviner.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(diviner);
        return diviner;
    }

    private void castDiviner() {
        harness.castFromHand(player1, new IxallisDiviner(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }

    private Permanent findDiviner() {
        return findPermanent(player1, "Ixalli's Diviner");
    }
}
