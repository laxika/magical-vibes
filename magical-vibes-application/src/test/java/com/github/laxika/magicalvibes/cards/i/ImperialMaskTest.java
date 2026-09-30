package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
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

@CardUsed({ImperialMask.class, Ghostfire.class})
class ImperialMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Imperial Mask gives its controller hexproof")
    void givesControllerHexproof() {
        harness.addToBattlefield(player1, new ImperialMask());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The team-only copy ability creates no token in a non-team game")
    void doesNotCreateTokenCopyWithoutTeammate() {
        harness.castFromHand(player1, new ImperialMask(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent's spell from targeting its controller")
    void preventsOpponentSpellFromTargetingController() {
        harness.addToBattlefield(player1, new ImperialMask());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Imperial Mask stops giving its controller hexproof when it leaves")
    void losesHexproofWhenRemoved() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new ImperialMask());
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mask);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }
}
