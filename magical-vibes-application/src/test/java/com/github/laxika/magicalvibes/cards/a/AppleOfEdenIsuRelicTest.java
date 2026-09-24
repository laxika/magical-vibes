package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AppleOfEdenIsuRelic.class, Forest.class, GrizzlyBears.class, Island.class})
class AppleOfEdenIsuRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target opponent's hand face down and lets its controller cast a card")
    void exilesHandAndCastsSpellWithOwnerDrawTrigger() {
        Permanent apple = addApple();
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        Island drawn = new Island();
        harness.setHand(player2, new ArrayList<>(List.of(spell, land)));
        harness.setLibrary(player2, List.of(drawn));

        activateApple(apple);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apple);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .allMatch(entry -> entry.faceDown() && apple.getId().equals(entry.sourcePermanentId()))
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(spell, land);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(land);
    }

    @Test
    @DisplayName("Playing an exiled land also makes its owner draw a card")
    void playingExiledLandTriggersOwnerDraw() {
        Permanent apple = addApple();
        Forest land = new Forest();
        Island drawn = new Island();
        harness.setHand(player2, List.of(land));
        harness.setLibrary(player2, List.of(drawn));

        activateApple(apple);
        harness.castFromExile(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    @DisplayName("Returns cards that were not played at the next end step")
    void returnsUnplayedCardsAtNextEndStep() {
        Permanent apple = addApple();
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setHand(player2, List.of(exiled));

        activateApple(apple);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);

        harness.forceStep(TurnStep.END_STEP);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(exiled);
    }

    private Permanent addApple() {
        Permanent apple = new Permanent(new AppleOfEdenIsuRelic());
        apple.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(apple);
        return apple;
    }

    private void activateApple(Permanent apple) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apple),
                null, player2.getId());
        harness.passBothPriorities();
    }
}
