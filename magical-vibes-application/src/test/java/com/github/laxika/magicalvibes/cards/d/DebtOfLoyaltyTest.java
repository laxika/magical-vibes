package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.FatalBlow;
import com.github.laxika.magicalvibes.cards.j.JabarisBanner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DebtOfLoyalty.class, BenalishKnight.class, JabarisBanner.class, FatalBlow.class})
class DebtOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Debt of Loyalty grants a regeneration shield but does not move the creature on resolution")
    void grantsShieldWithoutImmediateControlChange() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster gains control of the creature when the shield is actually spent")
    void gainsControlWhenShieldIsSpent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        bear.setMarkedDamage(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent stolen = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(stolen.getId()).isEqualTo(bear.getId());
        assertThat(stolen.isTapped()).isTrue();
        assertThat(stolen.getMarkedDamage()).isZero();
        assertThat(stolen.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Using another regeneration shield can leave the Debt of Loyalty shield available")
    void debtShieldRemainsWhenAnotherShieldIsUsed() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        bear.setRegenerationShield(bear.getRegenerationShield() + 1);
        bear.setMarkedDamage(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear);
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature's controller chooses which regeneration shield to use")
    void controllerChoosesAmongRegenerationShields() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        bear.setRegenerationShield(bear.getRegenerationShield() + 1);
        bear.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction())
                .as("the creature's controller must choose the regeneration shield")
                .isNotNull();
    }

    @Test
    @DisplayName("The control change does not happen when the creature cannot be regenerated")
    void doesNotGainControlWhenDestructionCannotBeRegenerated() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        bear.setMarkedDamage(1);
        gd.permanentsDealtDamageThisTurn.add(bear.getId());

        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(bear.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Debt of Loyalty")
    void cannotTargetNonCreature() {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new JabarisBanner());
        harness.setHand(player1, List.of(new DebtOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, banner.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
