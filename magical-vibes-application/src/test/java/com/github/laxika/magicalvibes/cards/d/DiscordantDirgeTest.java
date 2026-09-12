package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscordantDirge.class, Forest.class, Swamp.class})
class DiscordantDirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a verse counter on Discordant Dirge")
    void upkeepAcceptedAddsVerseCounter() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dirge.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not add a verse counter")
    void upkeepDeclinedDoesNotAddVerseCounter() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dirge.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during an opponent's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(dirge.getCounterCount(CounterType.VERSE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability sacrifices Discordant Dirge and discards up to its verse counters")
    void abilityDiscardsUpToVerseCounters() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());
        dirge.setCounterCount(CounterType.VERSE, 2);
        harness.setHand(player2, List.of(new Forest(), new Swamp(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Swamp");
        harness.assertInGraveyard(player1, "Discordant Dirge");
    }

    @Test
    @DisplayName("The controller may choose fewer than the number of verse counters")
    void mayChooseFewerThanVerseCounters() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());
        dirge.setCounterCount(CounterType.VERSE, 2);
        harness.setHand(player2, List.of(new Forest(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Swamp");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Discordant Dirge");
    }

    @Test
    @DisplayName("The activated ability lets only its controller look at the target hand")
    void abilityLooksAtTargetHandPrivately() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());
        dirge.setCounterCount(CounterType.VERSE, 1);
        harness.setHand(player2, List.of(new Forest(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("looks at " + player2.getUsername() + "'s hand.")).isTrue();
        assertThat(gameLogContains("reveals their hand")).isFalse();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Forest") && message.contains("Swamp"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();

        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("With no verse counters, the ability sacrifices without discarding")
    void zeroVerseCountersSacrificeWithoutDiscarding() {
        harness.addToBattlefield(player1, new DiscordantDirge());
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setHand(player2, List.of(forest, swamp));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, swamp);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Discordant Dirge");
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new DiscordantDirge());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
