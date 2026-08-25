package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmiumConfluence.class, CavernOfSouls.class, GhostlyPrison.class})
class CosmiumConfluenceTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    void repeatedCaveModeResolvesAllThreeSelections() {
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new CavernOfSouls());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 1);
        harness.castSorcery(player1, 0, modes);
        harness.passBothPriorities();

        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, cave)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, cave, Keyword.HASTE)).isTrue();
    }

    @Test
    void repeatedDestroyModeUsesOneTargetPerSelection() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2);
        harness.castSorcery(player1, 0, modes, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
