package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FavoredHoplite.class, Shock.class, GiantGrowth.class})
class FavoredHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Favored Hoplite puts a counter on it and prevents damage")
    void castingSpellThatTargetsHopliteTriggersHeroic() {
        harness.addToBattlefield(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hopliteId = harness.getPermanentId(player1, "Favored Hoplite");
        harness.castAndResolveInstant(player1, 0, hopliteId);
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

        harness.castAndResolveInstant(player1, 0, player2.getId());

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
        harness.castAndResolveInstant(player2, 0, hopliteId);

        Permanent hoplite = findPermanent(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each targeting spell triggers heroic and prevention applies to multiple damage sources")
    void repeatedSpellsAddCountersAndPreventAllDamage() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, hoplite.getId());
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hoplite.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, hoplite.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, hoplite.getId());
        harness.castAndResolveInstant(player2, 0, hoplite.getId());

        harness.assertOnBattlefield(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hoplite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage prevention expires at end of turn while the counter remains")
    void preventionExpiresAtEndOfTurn() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, hoplite.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, hoplite.getId());

        harness.assertOnBattlefield(player1, "Favored Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hoplite.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hoplite can die to damage before its heroic ability resolves")
    void damageInResponseToHeroicIsNotPrevented() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new FavoredHoplite());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hoplite.getId());
        harness.castAndResolveInstant(player2, 0, hoplite.getId());

        harness.assertNotOnBattlefield(player1, "Favored Hoplite");
        harness.assertInGraveyard(player1, "Favored Hoplite");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Favored Hoplite");
    }
}
