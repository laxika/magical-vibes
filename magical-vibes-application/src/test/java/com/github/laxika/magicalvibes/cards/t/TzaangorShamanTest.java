package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TzaangorShaman.class, LightningBolt.class})
class TzaangorShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the next instant or sorcery copy")
    void combatDamageCopiesNextInstantOrSorcery() {
        Permanent shaman = addCreatureReady(player1, new TzaangorShaman());
        shaman.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gameData.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Without combat damage, the ability does not set up a copy")
    void withoutCombatDamageDoesNotSetUpCopy() {
        addCreatureReady(player1, new TzaangorShaman());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }
}
