package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeamsplitterMage.class, GrizzlyBears.class, Shock.class, BountyOfMight.class})
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

    @Test
    void copyResolvesOnlyOnChosenCreature() {
        UUID mageId = addMageAndBears();
        List<UUID> bears = controlledBearIds();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mageId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getFirst());
        harness.passBothPriorities();
        assertThat(controlledBearIds()).containsExactly(bears.getLast());
        harness.assertOnBattlefield(player1, "Beamsplitter Mage");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Beamsplitter Mage");
        assertThat(controlledBearIds()).containsExactly(bears.getLast());
    }

    @Test
    void noChoiceOrCopyWhenLastOtherCreatureDiesInResponse() {
        UUID mageId = harness.addToBattlefieldAndReturn(player1, new BeamsplitterMage()).getId();
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mageId);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearId);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void repeatedTargetsAreAllChangedToChosenCreature() {
        UUID mageId = addMageAndBears();
        UUID bearId = controlledBearIds().getFirst();
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, List.of(mageId, mageId, mageId));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities();
        var bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(bearId)).findFirst().orElseThrow();
        var mage = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(mageId)).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(11);
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
    }

    private UUID addMageAndBears() {
        UUID mageId = harness.addToBattlefieldAndReturn(player1, new BeamsplitterMage()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        return mageId;
    }

    private List<UUID> controlledBearIds() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .map(permanent -> permanent.getId())
                .toList();
    }
}
