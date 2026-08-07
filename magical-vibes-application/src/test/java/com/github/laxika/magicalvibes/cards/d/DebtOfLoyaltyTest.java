package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Debt of Loyalty grants a regeneration shield but does not move the creature on resolution")
    void grantsShieldWithoutImmediateControlChange() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster gains control of the creature when the shield is actually spent")
    void gainsControlWhenShieldIsSpent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.getGameData().playerBattlefields.get(player2.getId()).getFirst().setMarkedDamage(2);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        Permanent stolen = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(stolen.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(stolen.isTapped()).isTrue();
        assertThat(stolen.getMarkedDamage()).isZero();
        assertThat(stolen.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A plain regeneration shield is consumed before the Debt of Loyalty shield")
    void plainShieldIsConsumedFirst() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        bear.setRegenerationShield(bear.getRegenerationShield() + 1);
        bear.setMarkedDamage(2);
        harness.passBothPriorities();

        Permanent survivor = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(survivor.getRegenerationShield()).isEqualTo(1);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Debt of Loyalty")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
