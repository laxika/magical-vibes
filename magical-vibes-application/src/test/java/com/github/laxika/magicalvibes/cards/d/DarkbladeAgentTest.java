package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkbladeAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Has no surveil reward before surveiling")
    void rewardIsInactiveBeforeSurveiling() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch and draws after dealing combat damage after surveiling")
    void gainsAbilitiesAfterSurveiling() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DimirInformant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The surveil reward resets at the start of the next turn")
    void rewardResetsAtStartOfNextTurn() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new DimirInformant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isFalse();
    }
}
