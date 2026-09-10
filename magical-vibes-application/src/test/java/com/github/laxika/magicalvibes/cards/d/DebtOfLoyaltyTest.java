package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.FatalBlow;
import com.github.laxika.magicalvibes.cards.j.JabarisBanner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
    void simultaneousDestructionCollectsAllShieldChoicesInActivePlayerOrder() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        for (Permanent creature : List.of(first, second)) {
            harness.setHand(player1, List.of(new DebtOfLoyalty()));
            harness.addMana(player1, ManaColor.WHITE, 3);
            harness.castAndResolveInstant(player1, 0, creature.getId());
            creature.setRegenerationShield(2);
        }
        harness.forceActivePlayer(player2);
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Regenerate without an additional effect");
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(first.getRegenerationShield()).isEqualTo(2);
        assertThat(second.getRegenerationShield()).isEqualTo(2);
        harness.handleListChoice(player1, "Regenerate without an additional effect");
        assertThat(first.getRegenerationShield()).isEqualTo(1);
        assertThat(second.getRegenerationShield()).isEqualTo(1);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(gd.chosenRegenerationShields).isEmpty();
    }

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
        harness.handleListChoice(player2, "Regenerate without an additional effect");

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
                .isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(bear.getRegenerationShield()).isEqualTo(2);
        assertThat(bear.getMarkedDamage()).isEqualTo(2);
        String debtShield = choice.options().stream()
                .filter(option -> option.contains(" control")).findFirst().orElseThrow();
        assertThatThrownBy(() -> harness.handleListChoice(player1, debtShield))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, debtShield);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.getGainControlRegenerationShields()).isEmpty();
        assertThat(bear.getMarkedDamage()).isZero();
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
