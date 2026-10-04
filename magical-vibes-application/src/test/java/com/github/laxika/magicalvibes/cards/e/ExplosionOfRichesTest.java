package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExplosionOfRiches.class, Forest.class})
class ExplosionOfRichesTest extends BaseCardTest {

    @Test
    void opponentAcceptingDrawsAndCausesOneDamageTriggerPerDraw() {
        Forest controllerDraw = new Forest();
        Forest opponentDraw = new Forest();
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));

        cast();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        harness.assertLife(player2, 10);
    }

    @Test
    void opponentDecliningStillLeavesMandatoryDrawAndDamage() {
        Forest controllerDraw = new Forest();
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(new Forest()));

        cast();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 15);
    }

    private void cast() {
        harness.setHand(player1, List.of(new ExplosionOfRiches()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
