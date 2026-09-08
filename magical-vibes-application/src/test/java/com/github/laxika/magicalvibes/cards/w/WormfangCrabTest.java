package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormfangCrab.class, GrizzlyBears.class, LlanowarElves.class})
class WormfangCrabTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses an eligible permanent you control to exile")
    void opponentChoosesPermanentToExile() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crab = castCrab();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId())
                .doesNotContain(crab.getId(), opponentPermanent.getId());

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, crab.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(first.getCard());
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability returns the exiled permanent")
    void leavesTheBattlefieldReturnsExiledPermanent() {
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent crab = castCrab();
        harness.handlePermanentChosen(player2, exiled.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crab));
        resolvePendingTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(exiled.getCard());
        assertThat(gd.exiledCards)
                .noneMatch(entry -> crab.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("If it leaves before its enters-the-battlefield ability resolves, the card stays exiled")
    void leavingBeforeEnterTriggerResolvesLeavesCardExiled() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new WormfangCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Wormfang Crab");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crab));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, crab.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactly(first.getCard());
    }

    @Test
    @DisplayName("Wormfang Crab cannot be blocked")
    void cannotBeBlocked() {
        Permanent crab = new Permanent(new WormfangCrab());
        crab.setSummoningSick(false);
        crab.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(crab);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crab);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castCrab() {
        harness.setHand(player1, List.of(new WormfangCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Wormfang Crab");
    }

    private void resolvePendingTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
