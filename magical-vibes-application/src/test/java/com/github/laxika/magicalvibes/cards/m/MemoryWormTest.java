package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MemoryWorm.class, GrizzlyBears.class})
class MemoryWormTest extends BaseCardTest {

    @Test
    @DisplayName("Paradox deals damage, makes the target player discard and draw, and adds a counter")
    void paradoxResolvesAllEffects() {
        Permanent worm = harness.addToBattlefieldAndReturn(player1, new MemoryWorm());
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        Permanent worm = harness.addToBattlefieldAndReturn(player1, new MemoryWorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void paradoxCanTargetController() {
        Permanent worm = harness.addToBattlefieldAndReturn(player1, new MemoryWorm());
        MemoryWorm spell = new MemoryWorm();
        MemoryWorm discarded = new MemoryWorm();
        MemoryWorm drawn = new MemoryWorm();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void emptyHandStillDrawsAndAddsCounter() {
        Permanent worm = harness.addToBattlefieldAndReturn(player1, new MemoryWorm());
        MemoryWorm spell = new MemoryWorm();
        MemoryWorm drawn = new MemoryWorm();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentCastingFromExileDoesNotTriggerParadox() {
        Permanent worm = harness.addToBattlefieldAndReturn(player1, new MemoryWorm());
        MemoryWorm spell = new MemoryWorm();
        harness.forceActivePlayer(player2);
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castFromExile(player2, spell.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(worm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
