package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkCaveDiver.class, BrazenBuccaneers.class, Forest.class, GrizzlyBears.class})
class MerfolkCaveDiverTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature explores, Merfolk Cave-Diver gets +1/+0 and can't be blocked")
    void exploreTriggersBoostAndUnblockable() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.getToughnessModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Explore trigger resolves after the nonland choice")
    void exploreNonlandTriggersAfterChoice() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isEqualTo(1);
        assertThat(caveDiver.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent caveDiver = harness.addToBattlefieldAndReturn(player1, new MerfolkCaveDiver());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(caveDiver.getPowerModifier()).isZero();
        assertThat(caveDiver.getToughnessModifier()).isZero();
        assertThat(caveDiver.isCantBeBlocked()).isFalse();
    }

    private void castExplorerAndResolveExplore() {
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
