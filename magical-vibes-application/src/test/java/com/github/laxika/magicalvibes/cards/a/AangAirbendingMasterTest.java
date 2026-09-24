package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangAirbendingMaster.class, GrizzlyBears.class})
class AangAirbendingMasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB airbends another creature")
    void airbendsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangAirbendingMaster()));
        addAangMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Gets one experience counter when one or more controlled creatures leave without dying")
    void gainsOneExperienceForMultipleControlledCreaturesLeavingWithoutDying() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().beginPermanentLeaveBatch(gd);
            try {
                harness.getPermanentRemovalService().removePermanentToHand(gd, first);
                harness.getPermanentRemovalService().removePermanentToHand(gd, second);
            } finally {
                harness.getPermanentRemovalService().endPermanentLeaveBatch(gd);
            }
        });
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("Does not get experience when a creature dies or an opponent's creature leaves")
    void ignoresDeathsAndOpponents() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentCreature));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Creates one Ally token for each experience counter at upkeep")
    void createsAllyTokensForExperienceCounters() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> allies = findPermanents(player1, "Ally");
        assertThat(allies).hasSize(2);
        assertThat(allies).allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ALLY));
    }

    private void addAangMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
