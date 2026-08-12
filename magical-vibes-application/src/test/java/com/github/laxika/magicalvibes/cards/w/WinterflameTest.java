package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WinterflameTest extends BaseCardTest {

    @Test
    @DisplayName("Tap mode taps the target creature")
    void tapModeTapsTargetCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castMode(0, List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage mode deals 2 damage to the target creature")
    void damageModeDealsTwoDamage() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castMode(1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Both modes can target the same creature")
    void bothModesTargetSameCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new Winterflame()));
        addWinterflameMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(target.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modes reject a noncreature target")
    void modesRejectNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Winterflame()));
        addWinterflameMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castMode(int modeIndex, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new Winterflame()));
        addWinterflameMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{modeIndex}, targetIds);
        harness.passBothPriorities();
    }

    private void addWinterflameMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
