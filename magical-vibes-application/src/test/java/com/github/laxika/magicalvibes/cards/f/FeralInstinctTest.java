package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralInstinct.class, PhyrexianWalker.class, SisaysRing.class})
class FeralInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives the target +1/+1 and schedules a draw instead of drawing now")
    void boostsAndSchedulesDraw() {
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.setHand(player1, List.of(new FeralInstinct()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID walkerId = harness.getPermanentId(player1, "Phyrexian Walker");
        harness.castInstant(player1, 0, walkerId);
        harness.passBothPriorities();

        Permanent walker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(walker.getEffectivePower()).isEqualTo(1);
        assertThat(walker.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.setHand(player1, List.of(new FeralInstinct()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID walkerId = harness.getPermanentId(player1, "Phyrexian Walker");
        harness.castInstant(player1, 0, walkerId);
        harness.passBothPriorities();

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.setHand(player1, List.of(new FeralInstinct()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID walkerId = harness.getPermanentId(player1, "Phyrexian Walker");
        harness.castInstant(player1, 0, walkerId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent walker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(walker.getEffectivePower()).isEqualTo(0);
        assertThat(walker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new PhyrexianWalker());
        harness.addToBattlefield(player1, new SisaysRing());
        harness.setHand(player1, List.of(new FeralInstinct()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Sisay's Ring");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
