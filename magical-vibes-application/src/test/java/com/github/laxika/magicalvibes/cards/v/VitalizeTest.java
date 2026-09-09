package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vitalize.class, BenalishInfantry.class, MindStone.class})
class VitalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all tapped creatures you control")
    void untapsAllTappedCreaturesYouControl() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        bear1.tap();
        bear2.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Leaves already untapped creatures untapped")
    void leavesAlreadyUntappedCreaturesUntapped() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        tappedCreature.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(untappedCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps every creature without prompting for a choice")
    void untapsWithoutPromptingForAChoice() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player1, new BenalishInfantry());
        gd.playerBattlefields.get(player1.getId()).forEach(Permanent::tap);

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        // "Untap all creatures you control" has no chosenCount, so it must not take the
        // "untap up to N" branch that Rewind and Unwind share with it.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        opponentBear.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents you control")
    void doesNotUntapNonCreaturePermanents() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        mindStone.tap();

        harness.castFromHand(player1, new Vitalize(), "{G}");
        harness.passBothPriorities();

        assertThat(mindStone.isTapped()).isTrue();
    }
}
