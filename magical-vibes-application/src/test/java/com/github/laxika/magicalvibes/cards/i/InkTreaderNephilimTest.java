package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InkTreaderNephilim.class, GrizzlyBears.class, Shock.class})
class InkTreaderNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell for every other legal creature and controls the copies")
    void copiesForEveryOtherCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting another permanent")
    void doesNotTriggerForAnotherPermanent() {
        harness.addToBattlefield(player1, new InkTreaderNephilim());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting a player")
    void doesNotTriggerForPlayerTarget() {
        harness.addToBattlefield(player1, new InkTreaderNephilim());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }
}
