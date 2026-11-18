package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.l.LeechFanatic;
import com.github.laxika.magicalvibes.cards.l.LashOfMalice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeamingDefiance.class, LeechFanatic.class, LashOfMalice.class})
class BeamingDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +2/+2 and hexproof")
    void givesBoostAndHexproof() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());

        castResolve(bears);

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(PermanentGrants.keywordsOf(gd, bears)).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());

        castResolve(bears);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, bears)).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LeechFanatic());
        harness.setHand(player1, List.of(new BeamingDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID opponentId = opponent.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Granted hexproof prevents opponents from targeting the creature")
    void hexproofPreventsOpponentTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());
        castResolve(creature);
        harness.setHand(player2, List.of(new LashOfMalice()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Granted hexproof still allows its controller to target the creature")
    void hexproofAllowsControllerTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());
        castResolve(creature);
        harness.setHand(player1, List.of(new LashOfMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Gaining hexproof makes an opponent's spell already on the stack fail to resolve")
    void hexproofStopsPendingOpponentSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());
        harness.setHand(player2, List.of(new LashOfMalice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, creature.getId());

        castResolve(creature);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new BeamingDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
