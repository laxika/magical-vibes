package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SachiDaughterOfSeshiro;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArenaOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps legendary creatures on every battlefield, but not other creatures or legendary lands")
    void entryTapsLegendaryCreaturesOnly() {
        Permanent ownLegend = addReady(player1, new SachiDaughterOfSeshiro());
        Permanent opponentLegend = addReady(player2, new SachiDaughterOfSeshiro());
        Permanent nonLegend = addReady(player1, new GrizzlyBears());
        Permanent legendaryLand = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        harness.setHand(player1, List.of(new ArenaOfTheAncients()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownLegend.isTapped()).isTrue();
        assertThat(opponentLegend.isTapped()).isTrue();
        assertThat(nonLegend.isTapped()).isFalse();
        assertThat(legendaryLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Legendary creatures stay tapped through each controller's untap step")
    void legendaryCreaturesDoNotUntap() {
        harness.addToBattlefield(player1, new ArenaOfTheAncients());
        Permanent ownLegend = addReady(player1, new SachiDaughterOfSeshiro());
        Permanent opponentLegend = addReady(player2, new SachiDaughterOfSeshiro());
        Permanent ownNonLegend = addReady(player1, new GrizzlyBears());
        ownLegend.tap();
        opponentLegend.tap();
        ownNonLegend.tap();

        advanceToNextTurn(player2);
        assertThat(ownLegend.isTapped()).isTrue();
        assertThat(ownNonLegend.isTapped()).isFalse();

        advanceToNextTurn(player1);
        assertThat(opponentLegend.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
