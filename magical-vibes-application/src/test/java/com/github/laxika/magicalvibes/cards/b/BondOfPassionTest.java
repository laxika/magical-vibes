package com.github.laxika.magicalvibes.cards.b;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondOfPassion.class, GrizzlyBears.class})
class BondOfPassionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, grants haste, and deals 2 damage to a player")
    void resolvesAllEffectsAgainstPlayer() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage to another creature")
    void dealsDamageToAnotherCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), damageTarget.getId()));
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Rejects duplicate targets")
    void rejectsDuplicateTargets() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
