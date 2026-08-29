package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarrinsCodexTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a page counter on Barrin's Codex")
    void upkeepAcceptedAddsPageCounter() {
        Permanent codex = harness.addToBattlefieldAndReturn(player1, new BarrinsCodex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(codex.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger adds no page counter")
    void upkeepDeclinedAddsNoPageCounter() {
        Permanent codex = harness.addToBattlefieldAndReturn(player1, new BarrinsCodex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(codex.getCounterCount(CounterType.PAGE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Barrin's Codex draws cards equal to its page counters")
    void sacrificeSelfDrawsCardsEqualToPageCounters() {
        Permanent codex = harness.addToBattlefieldAndReturn(player1, new BarrinsCodex());
        codex.setCounterCount(CounterType.PAGE, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barrin's Codex");
        harness.assertInGraveyard(player1, "Barrin's Codex");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }
}
