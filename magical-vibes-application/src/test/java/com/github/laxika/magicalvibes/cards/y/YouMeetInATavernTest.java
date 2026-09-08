package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouMeetInATavern.class, GrizzlyBears.class, LlanowarElves.class, Forest.class,
        Shock.class, Island.class})
class YouMeetInATavernTest extends BaseCardTest {

    @Test
    void formAPartyPutsAnyNumberOfRevealedCreaturesIntoHand() {
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card secondCreature = new LlanowarElves();
        Card secondNonCreature = new Forest();
        Card thirdNonCreature = new Shock();
        Card cardAfterTopFive = new Island();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature,
                secondNonCreature, thirdNonCreature, cardAfterTopFive));
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                cardAfterTopFive, nonCreature, secondNonCreature, thirdNonCreature);
    }

    @Test
    void startABrawlBoostsOnlyYourCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouMeetInATavern()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
