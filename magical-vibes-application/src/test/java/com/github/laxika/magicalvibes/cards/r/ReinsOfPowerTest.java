package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReinsOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and exchanges all creatures with the target opponent, granting haste")
    void untapsExchangesAndGrantsHaste() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        opposingCreature.tap();

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not exchange noncreatures")
    void doesNotExchangeNoncreatures() {
        Permanent ownLand = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(ownLand);
        Permanent opposingLand = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(opposingLand);

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castReins(player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(opposingCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castReins(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
