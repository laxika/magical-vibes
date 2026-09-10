package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, WindDrake.class, TimeEbb.class})
class TimeEbbTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Time Ebb targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        UUID targetId = target.getId();

        TimeEbb timeEbb = new TimeEbb();
        harness.setHand(player1, List.of(timeEbb));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(timeEbb);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        UUID landId = land.getId();

        harness.setHand(player1, List.of(new TimeEbb()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Time Ebb puts target creature on top of its owner's library")
    void resolvingPutsTargetCreatureOnTopOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        UUID targetId = target.getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        TimeEbb timeEbb = new TimeEbb();
        harness.setHand(player1, List.of(timeEbb));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getId()).isEqualTo(target.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(timeEbb.getId()));
    }

    @Test
    @DisplayName("Time Ebb fizzles if the target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        UUID targetId = target.getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        TimeEbb timeEbb = new TimeEbb();
        harness.setHand(player1, List.of(timeEbb));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(timeEbb.getId()));
    }
}
