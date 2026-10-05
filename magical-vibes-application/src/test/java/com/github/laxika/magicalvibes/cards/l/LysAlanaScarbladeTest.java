package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.e.ElvishBranchbender;
import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LysAlanaScarblade.class, CloudcrownOak.class, HillcomberGiant.class,
        ElvishBranchbender.class, EyeblightsEnding.class})
class LysAlanaScarbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -X/-X where X is the number of Elves controlled")
    void debuffsByElfCount() {
        addScarbladeReady(player1);
        harness.addToBattlefield(player1, new ElvishBranchbender());
        // Player1 controls 2 Elves: Lys Alana Scarblade and Elvish Branchbender
        harness.setHand(player1, List.of(new ElvishBranchbender()));

        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak()).getId();

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Cloudcrown Oak");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Only an Elf card is a valid discard cost")
    void discardCostRequiresElf() {
        addScarbladeReady(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak()).getId();
        harness.setHand(player1, List.of(new HillcomberGiant(), new ElvishBranchbender()));

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Cannot activate without an Elf card in hand")
    void cannotActivateWithoutElf() {
        addScarbladeReady(player1);
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak()).getId();
        harness.setHand(player1, List.of(new HillcomberGiant()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addScarbladeReady(player1);
        harness.setHand(player1, List.of(new ElvishBranchbender()));

        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak()).getId();

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Cloudcrown Oak");
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void countsElvesAtResolutionAndOnlyForAbilityController() {
        Permanent source = addScarbladeReady(player1);
        harness.setHand(player1, List.of(new ElvishBranchbender()));
        harness.addToBattlefield(player2, new ElvishBranchbender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(source.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Elvish Branchbender");
        harness.assertInGraveyard(player1, "Elvish Branchbender");
        harness.addToBattlefield(player1, new ElvishBranchbender());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void resolvesWithZeroElvesAfterSourceLeavesBattlefield() {
        Permanent source = addScarbladeReady(player1);
        harness.setHand(player1, List.of(new ElvishBranchbender()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void acceptsNoncreatureElfCardAsDiscardCost() {
        addScarbladeReady(player1);
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eyeblight's Ending");
        harness.assertNotInHand(player1, "Eyeblight's Ending");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void canTargetItselfAndDiesFromZeroToughness() {
        Permanent source = addScarbladeReady(player1);
        harness.setHand(player1, List.of(new ElvishBranchbender()));

        harness.activateAbility(player1, 0, null, source.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lys Alana Scarblade");
        harness.assertInGraveyard(player1, "Lys Alana Scarblade");
    }

    @Test
    void cannotPayTapCostWhileSummoningSick() {
        Permanent source = addScarbladeReady(player1);
        source.setSummoningSick(true);
        harness.setHand(player1, List.of(new ElvishBranchbender()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Elvish Branchbender");
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addScarbladeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LysAlanaScarblade());
        perm.setSummoningSick(false);
        return perm;
    }
}
