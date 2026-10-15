package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningBlow.class, BalduvianBears.class})
class LightningBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Grants first strike to target creature and schedules a draw at the next upkeep")
    void grantsFirstStrikeAndSchedulesDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The delayed draw belongs to the caster when targeting an opponent's creature")
    void delayedDrawBelongsToCasterWhenTargetingOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, target.getId());
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("An illegal creature target prevents the spell from scheduling a draw")
    void illegalTargetPreventsDelayedDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Blow");
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The delayed draw still happens if the creature leaves after the spell resolves")
    void creatureLeavingAfterResolutionDoesNotPreventDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("An upkeep in the same turn does not draw, and the next turn draws only once")
    void delayedDrawWaitsForNextTurnAndTriggersOnlyOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new LightningBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);

        gd.turnNumber++;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();

        gd.turnNumber++;
        advanceToUpkeep(player2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
