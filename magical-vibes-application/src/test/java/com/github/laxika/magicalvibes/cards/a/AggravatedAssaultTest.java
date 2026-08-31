package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AggravatedAssault.class, GrizzlyBears.class})
class AggravatedAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Activation untaps your creatures and grants an additional combat/main phase pair")
    void activationUntapsCreaturesAndGrantsAdditionalCombatMainPhasePair() {
        harness.addToBattlefield(player1, new AggravatedAssault());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation is restricted to sorcery speed")
    void activationIsRestrictedToSorcerySpeed() {
        harness.addToBattlefield(player1, new AggravatedAssault());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
