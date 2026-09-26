package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelDeceiver.class, Forest.class, KamiOfOldStone.class})
class CruelDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        addCreatureReady(player1, new CruelDeceiver());
        Card topCard = new KamiOfOldStone();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Looking at an empty library does not create a card-choice interaction")
    void lookingAtEmptyLibraryDoesNotPrompt() {
        addCreatureReady(player1, new CruelDeceiver());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealing a land makes damage from Cruel Deceiver destroy the damaged creature")
    void landRevealDestroysDamagedCreature() {
        addCreatureReady(player1, new CruelDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);

        // The 1/7 Kami survives 2 damage, so only the granted ability can kill it.
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Kami of Old Stone"));
    }

    @Test
    @DisplayName("Revealing a nonland card leaves the damaged creature alive")
    void nonlandRevealGrantsNothing() {
        addCreatureReady(player1, new CruelDeceiver());
        harness.setLibrary(player1, List.of(new KamiOfOldStone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Without the ability granted, damage from Cruel Deceiver does not destroy the creature")
    void withoutActivationNothingIsDestroyed() {
        addCreatureReady(player1, new CruelDeceiver());
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The granted destroy trigger applies only when Cruel Deceiver deals the damage")
    void grantedTriggerIsScopedToCruelDeceiver() {
        addCreatureReady(player1, new CruelDeceiver());
        addCreatureReady(player1, new KamiOfOldStone());
        Permanent deceiverBlocker = addCreatureReady(player2, new KamiOfOldStone());
        Permanent allyBlocker = addCreatureReady(player2, new KamiOfOldStone());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(allyBlocker)
                .doesNotContain(deceiverBlocker);
    }

    @Test
    @DisplayName("The reveal ability names the controller's own top card, never an opponent's")
    void revealReadsTheControllersOwnLibrary() {
        addCreatureReady(player1, new CruelDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new KamiOfOldStone()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Forest")).isTrue();
        assertThat(gameLogContains("Kami of Old Stone")).isFalse();
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addCreatureReady(player1, new CruelDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
