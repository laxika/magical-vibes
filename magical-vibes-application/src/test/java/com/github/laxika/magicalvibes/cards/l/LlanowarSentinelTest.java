package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LlanowarSentinel.class, GrizzlyBears.class})
class LlanowarSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Llanowar Sentinel creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast(3);

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.assertOnBattlefield(player1, "Llanowar Sentinel");

        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting may without enough mana does not search")
    void acceptingMayWithoutEnoughManaDoesNotSearch() {
        setupAndCast(3);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (can't pay)

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("can't pay {1}{G}"));
    }

    @Test
    @DisplayName("Accepting may with enough mana allows searching for Llanowar Sentinel")
    void acceptingMayWithEnoughManaAllowsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (pays mana, shows search)

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Llanowar Sentinel"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing Llanowar Sentinel from search puts it onto battlefield")
    void choosingSentinelPutsItOntoBattlefield() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (pays mana, shows search)

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(countSentinelsOnBattlefield()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("library is shuffled"));
    }

    @Test
    @DisplayName("Choosing not to find leaves the matching Sentinel in the library")
    void choosingNotToFindDoesNotPutSentinelOntoBattlefield() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("chooses not to take a card"));
    }

    @Test
    @DisplayName("A Sentinel found by the ability can chain its own entry ability")
    void foundSentinelCanChainEntryAbility() {
        setupAndCast(9);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities(); // resolve the fetched Sentinel's entry ability
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities(); // resolve the second fetched Sentinel's entry ability
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countSentinelsOnBattlefield()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void setupAndCast(int greenMana) {
        harness.castFromHand(player1, new LlanowarSentinel(), "{2}{G}");
        harness.addMana(player1, ManaColor.GREEN, greenMana - 3);
    }

    private void setupLibraryWithSentinels() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new LlanowarSentinel(),
                new LlanowarSentinel(),
                new GrizzlyBears()
        ));
    }

    private long countSentinelsOnBattlefield() {
        return countPermanents(player1, "Llanowar Sentinel");
    }
}
