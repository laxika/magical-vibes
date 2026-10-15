package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloraColossus.class, Forest.class, Shock.class})
class FloraColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Flora Colossus power and toughness equal its controller's lands")
    void ptEqualsControlledLands() {
        Permanent flora = harness.addToBattlefieldAndReturn(player1, new FloraColossus());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, flora)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flora)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flora Colossus updates when its controller's lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent flora = harness.addToBattlefieldAndReturn(player1, new FloraColossus());

        assertThat(gqs.getEffectivePower(gd, flora)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, flora)).isZero();

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, flora)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flora)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents cannot target Flora Colossus")
    void opponentCannotTargetWithSpells() {
        harness.addToBattlefield(player1, new Forest());
        Permanent flora = harness.addToBattlefieldAndReturn(player1, new FloraColossus());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, flora.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its controller can target Flora Colossus through hexproof")
    void controllerCanTargetWithSpells() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent flora = harness.addToBattlefieldAndReturn(player1, new FloraColossus());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, flora.getId());

        assertThat(flora.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Flora Colossus");
    }

    @Test
    @DisplayName("Flora Colossus dies on resolution when its controller has no lands")
    void diesWithNoControlledLands() {
        harness.setHand(player1, List.of(new FloraColossus()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flora Colossus");
        harness.assertInGraveyard(player1, "Flora Colossus");
    }
}
