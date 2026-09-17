package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AriaOfFlame.class, Shock.class, GrizzlyBears.class, LilianaVess.class, Opt.class})
class AriaOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gives each opponent 10 life")
    void enteringGivesOpponentLife() {
        harness.setLife(player2, 5);
        harness.setHand(player1, List.of(new AriaOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Instant and sorcery spells add verse counters and deal increasing damage")
    void spellsAddCountersAndDealIncreasingDamage() {
        Permanent aria = harness.addToBattlefieldAndReturn(player1, new AriaOfFlame());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(aria.getCounterCount(CounterType.VERSE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Aria of Flame")
    void creatureDoesNotTrigger() {
        Permanent aria = harness.addToBattlefieldAndReturn(player1, new AriaOfFlame());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(aria.getCounterCount(CounterType.VERSE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Aria of Flame can target a player or planeswalker, but not a creature")
    void targetsPlayerOrPlaneswalker() {
        harness.addToBattlefield(player1, new AriaOfFlame());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId(), planeswalker.getId())
                .doesNotContain(creature.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }
}
