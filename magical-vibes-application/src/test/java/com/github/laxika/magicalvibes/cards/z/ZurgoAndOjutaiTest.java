package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoAndOjutai.class, ShivanDragon.class, GrizzlyBears.class})
class ZurgoAndOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof during the turn it enters")
    void hasHexproofDuringEnteringTurn() {
        harness.setHand(player1, List.of(new ZurgoAndOjutai()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo and Ojutai");
        assertThat(gqs.hasKeyword(gd, zurgo, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zurgo, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A Dragon combat-damage trigger looks at three cards and may return a Dragon")
    void looksAtCardsAndMayReturnDragon() {
        addCreatureReady(player1, new ZurgoAndOjutai());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent nonDragon = addCreatureReady(player1, new GrizzlyBears());
        dragon.setAttacking(true);
        nonDragon.setAttacking(true);

        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandTopBottom(0, 1));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, dragon.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonDragon).doesNotContain(dragon);
    }
}
