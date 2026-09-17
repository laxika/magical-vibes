package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrainFreeze;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinderingTouch.class, BrainFreeze.class, ScornfulEgotist.class})
class HinderingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay {2}")
    void countersSpellWhenControllerCannotPay() {
        BrainFreeze brainFreeze = new BrainFreeze();
        harness.setLibrary(player2, List.of(new BrainFreeze(), new BrainFreeze(), new BrainFreeze()));
        harness.setHand(player1, List.of(brainFreeze));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brainFreeze.getId());
        resolveStack(false);

        harness.assertInGraveyard(player1, "Brain Freeze");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Resolves a spell when its controller pays {2}")
    void resolvesSpellWhenControllerPays() {
        BrainFreeze brainFreeze = new BrainFreeze();
        harness.setLibrary(player2, List.of(new BrainFreeze(), new BrainFreeze(), new BrainFreeze()));
        harness.setHand(player1, List.of(brainFreeze));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brainFreeze.getId());
        resolveStack(true);

        harness.assertInGraveyard(player1, "Brain Freeze");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters a spell when its controller declines to pay {2}")
    void countersSpellWhenControllerDeclinesToPay() {
        BrainFreeze brainFreeze = new BrainFreeze();
        harness.setLibrary(player2, List.of(new BrainFreeze(), new BrainFreeze(), new BrainFreeze()));
        harness.setHand(player1, List.of(brainFreeze));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brainFreeze.getId());
        resolveStack(false);

        harness.assertInGraveyard(player1, "Brain Freeze");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
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
        BrainFreeze brainFreeze = new BrainFreeze();
        harness.setHand(player1, List.of(brainFreeze));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brainFreeze.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    @DisplayName("Storm counts spells cast by both players")
    void stormCountsSpellsCastByBothPlayers() {
        BrainFreeze firstSpell = new BrainFreeze();
        BrainFreeze secondSpell = new BrainFreeze();
        HinderingTouch hinderingTouch = new HinderingTouch();

        harness.setHand(player1, List.of(firstSpell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(secondSpell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(hinderingTouch));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, secondSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Can target only a spell on the stack")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        harness.setHand(player2, List.of(new HinderingTouch()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
    }
}
