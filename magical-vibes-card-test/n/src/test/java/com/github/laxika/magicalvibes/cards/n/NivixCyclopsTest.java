package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UncoveredClues;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivixCyclops.class, Shock.class, GrizzlyBears.class, UncoveredClues.class})
class NivixCyclopsTest extends BaseCardTest {

    private Permanent addCyclops(Player player) {
        return addCreatureReady(player, new NivixCyclops());
    }

    @Test
    @DisplayName("Cannot attack without a spell cast (defender)")
    void cannotAttackWithDefender() {
        addCyclops(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Casting an instant gives +3/+0 and lets it attack this turn")
    void castingInstantBoostsAndAllowsAttack() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isEqualTo(3);
        assertThat(cyclops.getToughnessModifier()).isEqualTo(0);
        assertThat(cyclops.getEffectivePower()).isEqualTo(4);

        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        assertThat(cyclops.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Boost and attack permission wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(cyclops.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(cyclops.getPowerModifier()).isEqualTo(0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isEqualTo(0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void sorceryTriggerResolvesBeforeTheSpell() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(cyclops.getEffectivePower()).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void repeatedCastsGiveCumulativeBoosts() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cyclops.getEffectivePower()).isEqualTo(7);
        assertThat(cyclops.getToughnessModifier()).isZero();
    }

    @Test
    void opponentCastingInstantDoesNotTrigger() {
        Permanent cyclops = addCyclops(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void attackPermissionDoesNotBypassSummoningSickness() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new NivixCyclops());
        cyclops.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isEqualTo(3);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
