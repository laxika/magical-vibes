package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaleSwooper.class, AlpineWatchdog.class})
class GaleSwooperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants flying to target creature")
    void etbGrantsFlying() {
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("ETB can target an opponent's creature")
    void etbCanTargetOpponentCreature() {
        harness.addToBattlefield(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();
        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("ETB fizzles if its target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("ETB grants flying only to the chosen creature")
    void onlyChosenCreatureGainsFlying() {
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.addToBattlefield(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Alpine Watchdog");
        UUID otherId = harness.getPermanentId(player2, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(permanent(otherId).hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("ETB resolves even when Gale Swooper leaves before resolution")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new GaleSwooper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Alpine Watchdog");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Gale Swooper");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(sourceId));
        resolveAllTriggers();

        assertThat(permanent(targetId).hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent permanent(UUID id) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }
}
