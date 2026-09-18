package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZedruuTheGreathearted.class, Donate.class, GrizzlyBears.class})
class ZedruuTheGreatheartedTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and draws for permanents it owns that opponents control")
    void gainsLifeAndDrawsForOwnedPermanentsOpponentsControl() {
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(player2.getId(), donated.getId()));
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new ZedruuTheGreathearted());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not count permanents owned by an opponent")
    void doesNotCountOpponentOwnedPermanents() {
        harness.addToBattlefield(player1, new ZedruuTheGreathearted());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Target opponent gains control of a permanent you control")
    void targetOpponentGainsControlOfPermanent() {
        Permanent zedruu = harness.addToBattlefieldAndReturn(player1, new ZedruuTheGreathearted());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(zedruu), 0,
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a player other than an opponent")
    void cannotTargetYourself() {
        Permanent zedruu = harness.addToBattlefieldAndReturn(player1, new ZedruuTheGreathearted());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(zedruu), 0,
                List.of(player1.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
