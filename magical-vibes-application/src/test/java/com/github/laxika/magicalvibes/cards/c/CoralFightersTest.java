package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralFighters.class, FemerefScouts.class, ViashinoWarrior.class, Forest.class})
class CoralFightersTest extends BaseCardTest {

    private void addAttacker() {
        addCreatureReady(player1, new CoralFighters());
    }

    private void setDefenderLibrary() {
        harness.setLibrary(player2, List.of(new FemerefScouts(), new ViashinoWarrior(), new Forest()));
    }

    private void attackUnblocked() {
        declareAttackers(List.of(0));

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<String> libraryNames() {
        return gd.playerDecks.get(player2.getId()).stream().map(Card::getName).toList();
    }

    @Test
    @DisplayName("Accepting the may puts the looked-at top card on the bottom of the defender's library")
    void unblockedAcceptBottomsTopCard() {
        setDefenderLibrary();
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        // Accepting puts the ability on the stack; let it resolve.
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Viashino Warrior", "Forest", "Femeref Scouts");
    }

    @Test
    @DisplayName("Declining the may leaves the defender's library untouched")
    void unblockedDeclineLeavesLibraryUntouched() {
        setDefenderLibrary();
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Femeref Scouts", "Viashino Warrior", "Forest");
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        setDefenderLibrary();

        addCreatureReady(player2, new FemerefScouts());

        addAttacker();

        declareAttackers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Femeref Scouts", "Viashino Warrior", "Forest");
    }

    @Test
    @DisplayName("Empty defender library presents no choice")
    void emptyLibraryNoChoice() {
        harness.setLibrary(player2, List.of());
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
