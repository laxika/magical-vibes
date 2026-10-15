package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmiteTheDeathless.class, DarksteelColossus.class, DarksteelMyr.class})
class SmiteTheDeathlessTest extends BaseCardTest {

    @Test
    void dealsThreeDamageAndRemovesIndestructibleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());
        castSmite(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void exilesIndestructibleCreatureThatWouldDieThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        castSmite(target);

        harness.assertNotOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Darksteel Myr"));
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, java.util.List.of(new SmiteTheDeathless()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    private void castSmite(Permanent target) {
        harness.setHand(player1, java.util.List.of(new SmiteTheDeathless()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
