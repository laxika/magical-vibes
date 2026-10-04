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
    @DisplayName("A nontoken Imperial Mask triggers even when there are no teammates")
    void triggersWithoutTeammates() {
        harness.castFromHand(player1, new ImperialMask(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A token Imperial Mask grants hexproof without triggering its copy ability")
    void tokenGrantsHexproofWithoutTriggering() {
        ImperialMask token = new ImperialMask();
        token.setToken(true);

        harness.enterBattlefieldAndReturn(player1, token);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Hexproof allows its controller to target themselves")
    void allowsControllerToTargetThemselves() {
        harness.addToBattlefield(player1, new ImperialMask());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gaining hexproof before resolution makes an opposing spell's target illegal")
    void gainingHexproofMakesPendingTargetIllegal() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, player1.getId());

        harness.addToBattlefield(player1, new ImperialMask());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Ghostfire");
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
