package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpellSatchelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a book counter on Spell Satchel")
    void castingInstantAddsBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an instant puts another book counter on Spell Satchel")
    void copyingInstantAddsBookCounter() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        Permanent conspireA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent conspireB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a book counter adds a colorless mana")
    void removingBookCounterAddsColorlessMana() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(manaBefore + 1);
    }

    @Test
    @DisplayName("Removing three book counters draws a card")
    void removingThreeBookCountersDrawsCard() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(satchel.getCounterCount(CounterType.BOOK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The draw ability requires three book counters")
    void drawAbilityRequiresThreeBookCounters() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new SpellSatchel());
        satchel.setCounterCount(CounterType.BOOK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
