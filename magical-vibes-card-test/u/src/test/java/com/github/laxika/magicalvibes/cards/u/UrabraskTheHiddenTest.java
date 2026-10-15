package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrabraskTheHidden.class, GrizzlyBears.class})
class UrabraskTheHiddenTest extends BaseCardTest {

    

    @Test
    @DisplayName("Controller's creatures have haste")
    void controllersCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do NOT have haste")
    void opponentsCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Urabrask grants haste to itself when it resolves")
    void urabraskHasHaste() {
        harness.setHand(player1, List.of(new UrabraskTheHidden()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent urabrask = findPermanent(player1, "Urabrask the Hidden");
        assertThat(gqs.hasKeyword(gd, urabrask, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures enter tapped even without being cast")
    void opponentsCreaturesEnterTappedWithoutBeingCast() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Existing opposing creatures are not tapped when Urabrask enters")
    void existingOpposingCreaturesRemainUntapped() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new UrabraskTheHidden());

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Both static effects end when Urabrask leaves the battlefield")
    void effectsEndWhenUrabraskLeaves() {
        Permanent urabrask = harness.addToBattlefieldAndReturn(player1, new UrabraskTheHidden());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
        assertThat(opposingBears.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(urabrask);
        gd.playerGraveyards.get(player1.getId()).add(urabrask.getCard());

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isFalse();
        assertThat(opposingBears.isTapped()).isTrue();
        Permanent newlyEnteringBears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(newlyEnteringBears.isTapped()).isFalse();
    }
}
