package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianAcademy.class, VoltaicKey.class})
class TolarianAcademyTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one blue mana for each artifact its controller controls")
    void addsBlueManaForControlledArtifacts() {
        Permanent academy = harness.addToBattlefieldAndReturn(player1, new TolarianAcademy());
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.addToBattlefield(player2, new VoltaicKey());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(academy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when its controller controls no artifacts")
    void addsNoManaWithoutArtifacts() {
        harness.addToBattlefield(player1, new TolarianAcademy());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Counts tapped artifacts but ignores artifacts outside the battlefield")
    void countsOnlyArtifactsOnBattlefieldRegardlessOfTappedState() {
        harness.addToBattlefield(player1, new TolarianAcademy());
        Permanent key = harness.addToBattlefieldAndReturn(player1, new VoltaicKey());
        key.tap();
        harness.setHand(player1, List.of(new VoltaicKey()));
        harness.setGraveyard(player1, List.of(new VoltaicKey()));
        harness.setExile(player1, List.of(new VoltaicKey()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate again while tapped and recounts artifacts after untapping")
    void requiresUntappingAndRecountsArtifactsForEachActivation() {
        harness.addToBattlefield(player1, new TolarianAcademy());
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.performUntapStep(player1);
        harness.addToBattlefield(player1, new VoltaicKey());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
