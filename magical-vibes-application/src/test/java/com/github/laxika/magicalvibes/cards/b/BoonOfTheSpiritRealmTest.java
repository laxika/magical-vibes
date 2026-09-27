package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BoonOfTheSpiritRealm.class, GloriousAnthem.class, GrizzlyBears.class})
class BoonOfTheSpiritRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry puts a blessing counter on it and boosts creatures you control")
    void ownEntryAddsBlessingAndBoostsCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoonOfTheSpiritRealm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
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
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering does not add a blessing counter")
    void creatureEntryDoesNotTrigger() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not add a blessing counter")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new BoonOfTheSpiritRealm());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boon.getCounterCount(CounterType.BLESSING)).isZero();
    }
}
