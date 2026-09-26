package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterbendersRestoration.class, GrizzlyBears.class})
class WaterbendersRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X controlled creatures and returns them at the next end step")
    void exilesExactlyXCreaturesAndReturnsThemAtNextEndStep() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent waterbendSourceOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent waterbendSourceTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(waterbendSourceOne.getId(), waterbendSourceTwo.getId()));
        assertThat(waterbendSourceOne.isTapped()).isTrue();
        assertThat(waterbendSourceTwo.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(untargeted)
                .doesNotContain(firstTarget, secondTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(untargeted)
                .hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Allows X to be zero")
    void allowsXToBeZero() {
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaterbendersRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
