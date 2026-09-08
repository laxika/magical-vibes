package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("The ability sacrifices Discordant Dirge and discards up to its verse counters")
    void abilityDiscardsUpToVerseCounters() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());
        dirge.setCounterCount(CounterType.VERSE, 2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel(), new GrizzlyBears()));
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
                .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
        harness.assertInGraveyard(player1, "Discordant Dirge");
    }

    @Test
    @DisplayName("The controller may choose fewer than the number of verse counters")
    void mayChooseFewerThanVerseCounters() {
        Permanent dirge = harness.addToBattlefieldAndReturn(player1, new DiscordantDirge());
        dirge.setCounterCount(CounterType.VERSE, 2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Serra Angel");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
