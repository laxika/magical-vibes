package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class IxallisDivinerTest extends BaseCardTest {

    // ===== Explore reveals a land — put into hand =====

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

    // ===== Explore reveals a non-land — +1/+1 counter and may graveyard =====

    @Test
    @DisplayName("Explore with non-land on top puts +1/+1 counter on creature")
    void exploreNonLandAddsCounter() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castDiviner();

        Permanent diviner = findDiviner();
        assertThat(diviner).isNotNull();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land on top prompts may ability")
    void exploreNonLandPromptsMayAbility() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castDiviner();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Explore non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Card creature = new GrizzlyBears();
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
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        castDiviner();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    // ===== Explore with empty library =====

    @Test
    @DisplayName("Explore with empty library does nothing")
    void exploreEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        castDiviner();

        Permanent diviner = findDiviner();
        assertThat(diviner).isNotNull();
        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Helpers =====

    private void castDiviner() {
        harness.setHand(player1, List.of(new IxallisDiviner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }

    private Permanent findDiviner() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ixalli's Diviner"))
                .findFirst().orElse(null);
    }
}
