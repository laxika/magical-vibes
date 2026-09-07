package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildfireEmissary.class, EkunduGriffin.class, Pacifism.class, Regeneration.class})
class WildfireEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R} pumps Wildfire Emissary by +1/+0")
    void pumpAbilityBoostsPower() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.getPowerModifier()).isEqualTo(1);
        assertThat(emissary.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The pump ability stacks when activated twice")
    void pumpAbilityStacks() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The pump ability does not require Wildfire Emissary to tap")
    void pumpAbilityDoesNotRequireTapping() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        emissary.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.isTapped()).isTrue();
        assertThat(emissary.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the pump ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the pump ability with only colorless mana")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The pump bonus wears off at end of turn")
    void pumpBonusWearsOffAtEndOfTurn() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(emissary.getPowerModifier()).isZero();
        assertThat(emissary.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player2, new WildfireEmissary());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, emissary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by a non-white spell")
    void canBeTargetedByNonWhiteSpell() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new WildfireEmissary());

        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, emissary.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("White creatures cannot block Wildfire Emissary")
    void whiteCreatureCannotBlock() {
        Permanent emissary = addCreatureReady(player1, new WildfireEmissary());
        Permanent whiteCreature = addCreatureReady(player2, new EkunduGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(whiteCreature),
                gd.playerBattlefields.get(player1.getId()).indexOf(emissary)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
