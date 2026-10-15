package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SomberwaldAlpha.class, DwynensElite.class})
class SomberwaldAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control that becomes blocked gets +1/+1 until end of turn")
    void allyBecomesBlockedGetsBoost() {
        Permanent bears = addCreatureReady(player1, new DwynensElite());
        bears.setAttacking(true);
        addReadyAlpha(player1);
        addCreatureReady(player2, new DwynensElite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("No trigger when the attacker is unblocked")
    void unblockedCreatesNoTrigger() {
        Permanent bears = addCreatureReady(player1, new DwynensElite());
        bears.setAttacking(true);
        addReadyAlpha(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("{1}{G} grants trample to a creature you control until end of turn")
    void abilityGrantsTrample() {
        addReadyAlpha(player1);
        Permanent target = addCreatureReady(player1, new DwynensElite());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyAlpha(player1);
        Permanent enemy = addCreatureReady(player2, new DwynensElite());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alphaBoostsItselfWhenBlockedAndBoostExpires() {
        Permanent alpha = addReadyAlpha(player1);
        alpha.setAttacking(true);
        addReadyAlpha(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(alpha.getPowerModifier()).isEqualTo(1);
        assertThat(alpha.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(alpha.getPowerModifier()).isZero();
        assertThat(alpha.getToughnessModifier()).isZero();
    }

    @Test
    void multipleBlockersProduceOnlyOneBoost() {
        Permanent alpha = addReadyAlpha(player1);
        alpha.setAttacking(true);
        addReadyAlpha(player2);
        addReadyAlpha(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(alpha.getPowerModifier()).isEqualTo(1);
        assertThat(alpha.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void multipleAlphasEachBoostTheBlockedCreature() {
        Permanent attacker = addReadyAlpha(player1);
        attacker.setAttacking(true);
        Permanent otherAlpha = addReadyAlpha(player1);
        addReadyAlpha(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
        assertThat(otherAlpha.getPowerModifier()).isZero();
        assertThat(otherAlpha.getToughnessModifier()).isZero();
    }

    @Test
    void opposingAlphaDoesNotBoostBlockedOpponent() {
        Permanent attacker = addCreatureReady(player1, new DwynensElite());
        attacker.setAttacking(true);
        Permanent defendingAlpha = addReadyAlpha(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(defendingAlpha.getPowerModifier()).isZero();
    }

    @Test
    void tappedSummoningSickAlphaCanGrantItselfTrample() {
        Permanent alpha = harness.addToBattlefieldAndReturn(player1, new SomberwaldAlpha());
        alpha.setSummoningSick(true);
        alpha.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, alpha.getId());
        resolveAllTriggers();

        assertThat(alpha.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(alpha.isTapped()).isTrue();
    }

    private Permanent addReadyAlpha(Player player) {
        return addCreatureReady(player, new SomberwaldAlpha());
    }
}
