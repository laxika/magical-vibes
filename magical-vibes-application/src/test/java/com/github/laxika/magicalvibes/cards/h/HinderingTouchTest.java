package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HinderingTouch.class, GrizzlyBears.class, MightOfOaks.class})
class HinderingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay {2}")
    void countersSpellWhenControllerCannotPay() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        resolveStack(false);

        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Leaves a spell on the stack when its controller pays {2}")
    void leavesSpellOnStackWhenControllerPays() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        resolveStack(true);

        assertThat(bear.getPowerModifier()).isEqualTo(7);
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    private void resolveStack(boolean payCounterUnless) {
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            PendingInteraction.MayAbilityChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
            if (choice == null) {
                harness.passBothPriorities();
            } else if (choice.playerId().equals(player1.getId())) {
                harness.handleMayAbilityChosen(player1, payCounterUnless);
            } else {
                harness.handleMayAbilityChosen(player2, false);
            }
        }
    }

    @Test
    @DisplayName("Storm creates a copy for the spell cast before Hindering Touch")
    void stormCreatesCopyForEachPriorSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }
}
