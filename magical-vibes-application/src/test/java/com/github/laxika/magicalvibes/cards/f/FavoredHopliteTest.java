package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FavoredHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Favored Hoplite puts a counter on it and prevents damage")
    void castingSpellThatTargetsHopliteTriggersHeroic() {
        harness.addToBattlefield(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hopliteId = harness.getPermanentId(player1, "Favored Hoplite");
        harness.castInstant(player1, 0, hopliteId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hoplite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Favored Hoplite")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Favored Hoplite does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new FavoredHoplite());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID hopliteId = harness.getPermanentId(player1, "Favored Hoplite");
        harness.castInstant(player2, 0, hopliteId);
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
