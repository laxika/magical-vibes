package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoonOfTheSpiritRealm.class, GloriousAnthem.class, GrizzlyBears.class, Demystify.class})
class BoonOfTheSpiritRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry puts a blessing counter on it and boosts creatures you control")
    void ownEntryAddsBlessingAndBoostsCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BoonOfTheSpiritRealm(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent boon = findPermanent(player1, "Boon of the Spirit Realm");
        assertThat(boon.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Another enchantment entering adds another blessing counter")
    void anotherEnchantmentAddsBlessing() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering does not add a blessing counter")
    void creatureEntryDoesNotTrigger() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not add a blessing counter")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isZero();
    }

    @Test
    @DisplayName("Two Boons accumulate independent counters and their boosts add together")
    void multipleBoonsAccumulateIndependentCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new BoonOfTheSpiritRealm(), "{3}{W}{W}");
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Boon of the Spirit Realm");
        assertThat(first.getCounterCount(CounterType.BLESSING)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.passBothPriorities();

        harness.castFromHand(player1, new BoonOfTheSpiritRealm(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BoonOfTheSpiritRealm)
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(first.getCounterCount(CounterType.BLESSING)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent laterBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .filter(permanent -> !permanent.getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(5);
        assertThat(first.getCounterCount(CounterType.BLESSING)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Boon removes its boost immediately")
    void removingBoonRemovesBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BoonOfTheSpiritRealm(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent boon = findPermanent(player1, "Boon of the Spirit Realm");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, boon.getId());

        harness.assertInGraveyard(player1, "Boon of the Spirit Realm");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the entering enchantment does not stop an already triggered blessing")
    void triggerResolvesAfterEnteringEnchantmentLeaves() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        Permanent anthem = findPermanent(player1, "Glorious Anthem");
        assertThat(boon.getCounterCount(CounterType.BLESSING)).isZero();

        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, anthem.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(boon.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
    }
}
