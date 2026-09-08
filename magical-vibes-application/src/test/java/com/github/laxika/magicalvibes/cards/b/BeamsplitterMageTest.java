package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BeamsplitterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell targeting only Beamsplitter Mage prompts for another creature")
    void targetingMagePromptsForAnotherCreature() {
        UUID mageId = addMageAndBears();
        List<UUID> bearIds = controlledBearIds();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mageId);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(bearIds);
    }

    @Test
    @DisplayName("Chosen creature receives the copied spell")
    void chosenCreatureReceivesCopy() {
        UUID mageId = addMageAndBears();
        UUID chosenBearId = controlledBearIds().getLast();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mageId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenBearId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(chosenBearId);
    }

    @Test
    @DisplayName("Opponent casting at Beamsplitter Mage does not trigger its ability")
    void opponentCastDoesNotTrigger() {
        UUID mageId = addMageAndBears();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, mageId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger is created when no other creature can be targeted")
    void noTriggerWithoutAnotherLegalCreature() {
        harness.addToBattlefield(player1, new BeamsplitterMage());
        UUID mageId = harness.getPermanentId(player1, "Beamsplitter Mage");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mageId);

        assertThat(gd.stack).hasSize(1);
    }

    private UUID addMageAndBears() {
        harness.addToBattlefield(player1, new BeamsplitterMage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        return harness.getPermanentId(player1, "Beamsplitter Mage");
    }

    private List<UUID> controlledBearIds() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .map(permanent -> permanent.getId())
                .toList();
    }
}
