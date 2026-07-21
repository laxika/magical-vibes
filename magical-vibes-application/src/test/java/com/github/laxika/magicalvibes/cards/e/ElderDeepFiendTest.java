package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElderDeepFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, taps up to four chosen permanents")
    void hardcastTapsUpToFourPermanents() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent c = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent d = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, a.getId());
        harness.handlePermanentChosen(player1, b.getId());
        harness.handlePermanentChosen(player1, c.getId());
        harness.handlePermanentChosen(player1, d.getId()); // max 4 — trigger on stack

        harness.passBothPriorities(); // resolve cast trigger
        harness.passBothPriorities(); // resolve creature spell

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        assertThat(c.isTapped()).isTrue();
        assertThat(d.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elder Deep-Fiend"));
    }

    @Test
    @DisplayName("Cast trigger can choose fewer than four targets (stop early)")
    void castTriggerCanStopEarly() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, a.getId());
        harness.handlePermanentChosen(player1, player1.getId()); // stop after one

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cast trigger can choose zero targets")
    void castTriggerCanChooseZero() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId()); // stop with zero

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elder Deep-Fiend"));
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        // Emerge {5}{U}{U} reduced by 2 → {3}{U}{U}
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponent.getId());
        // Only one legal permanent left after sacrifice — choosing it completes targeting.

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponent.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elder Deep-Fiend"))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        // Need {3}{U}{U} after reduction; only {2}{U}{U} available
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves even if the creature spell is still on the stack")
    void castTriggerResolvesBeforeCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spare = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId()); // stop; leave spare untapped

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(bears.isTapped()).isTrue();
        assertThat(spare.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elder Deep-Fiend"));
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Elder Deep-Fiend");
    }
}
