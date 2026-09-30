package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarisiBreakerOfTheCoil.class, GiantGrowth.class, GrizzlyBears.class})
class MarisiBreakerOfTheCoilTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot cast spells during combat")
    void playersCannotCastSpellsDuringCombat() {
        harness.addToBattlefield(player1, new MarisiBreakerOfTheCoil());
        Permanent player1Target = addCreatureReady(player1, new GrizzlyBears());
        Permanent player2Target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player2Target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> harness.castInstant(player1, 0, player1Target.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Combat damage goads every creature controlled by the damaged player")
    void combatDamageGoadsDamagedPlayersCreatures() {
        addCreatureReady(player1, new MarisiBreakerOfTheCoil());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent damagedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherDamagedCreature = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, damagedCreature)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, otherDamagedCreature)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, ownCreature)).isZero();
    }
}
