package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeEbb.class, Forest.class, WindDrake.class})
class TimeEbbTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Time Ebb targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new WindDrake());
        UUID targetId = harness.getPermanentId(player2, "Wind Drake");

        harness.setHand(player1, List.of(new TimeEbb()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getCard()).isInstanceOf(TimeEbb.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new TimeEbb()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Time Ebb puts target creature on top of its owner's library")
    void resolvingPutsTargetCreatureOnTopOfOwnersLibrary() {
        Card targetCard = new WindDrake();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, targetCard).getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new TimeEbb()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertNotInGraveyard(player2, "Wind Drake");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst()).isSameAs(targetCard);
        harness.assertInGraveyard(player1, "Time Ebb");
    }

    @Test
    @DisplayName("Time Ebb fizzles if the target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new WindDrake());
        UUID targetId = harness.getPermanentId(player2, "Wind Drake");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new TimeEbb()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Time Ebb");
    }
}
