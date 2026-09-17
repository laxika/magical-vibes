package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemporalFissure.class, GoblinBrigand.class})
class TemporalFissureTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target permanent to its owner's hand")
    void returnsTargetPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());

        castTemporalFissure(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Goblin Brigand");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Temporal Fissure")
    void stormCreatesCopiesForEachPriorSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        gd.recordSpellCast(player1.getId(), new GoblinBrigand());
        gd.recordSpellCast(player2.getId(), new GoblinBrigand());

        castTemporalFissure(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm copy returns the original target when it is not retargeted")
    void stormCopyReturnsOriginalTargetWhenNotRetargeted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        gd.recordSpellCast(player1.getId(), new GoblinBrigand());

        castTemporalFissure(target.getId());
        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm copy may be retargeted to another permanent")
    void stormCopyMayBeRetargetedToAnotherPermanent() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent alternateTarget = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        gd.recordSpellCast(player1.getId(), new GoblinBrigand());

        castTemporalFissure(originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, alternateTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(alternateTarget.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(originalTarget.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private void castTemporalFissure(UUID targetId) {
        harness.setHand(player1, List.of(new TemporalFissure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, targetId);
    }
}
