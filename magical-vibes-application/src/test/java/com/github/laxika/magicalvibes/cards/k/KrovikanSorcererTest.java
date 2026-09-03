package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrovikanSorcerer.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class,
        ScatheZombies.class})
class KrovikanSorcererTest extends BaseCardTest {

    // ===== Ability 0: discard a nonblack card, draw a card =====

    @Test
    @DisplayName("Nonblack ability only allows discarding nonblack cards as cost")
    void nonblackAbilityRestrictsToNonblackCards() {
        addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears(), new ScatheZombies()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        // Only the green Grizzly Bears (index 0) is a legal discard; the black Scathe Zombies is not.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Nonblack ability allows colorless cards")
    void nonblackAbilityAllowsColorlessCards() {
        Permanent sorcerer = addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(sorcerer.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
    }

    @Test
    @DisplayName("Discarding a nonblack card pays the cost and draws one card")
    void nonblackAbilityDrawsOneCard() {
        Permanent sorcerer = addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(sorcerer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        // Discarded the only card, then drew one: hand size back to 1.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    // ===== Ability 1: discard a black card, draw two then discard one =====

    @Test
    @DisplayName("Black ability only allows discarding black cards as cost")
    void blackAbilityRestrictsToBlackCards() {
        addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears(), new ScatheZombies()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        // Only the black Scathe Zombies (index 1) is a legal discard.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Discarding a black card draws two cards then discards one")
    void blackAbilityDrawsTwoThenDiscardsOne() {
        addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0); // pay cost — discard Scathe Zombies

        harness.assertInGraveyard(player1, "Scathe Zombies");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve — draws two cards

        // Loot draw done, now must discard one of the two.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Discarded 1 (cost) + drew 2 - discarded 1 = net hand size 1.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Black ability only allows discarding one of the two cards it drew")
    void blackAbilityRestrictsDiscardToDrawnCards() {
        addCreatureReady(player1, new KrovikanSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

}
