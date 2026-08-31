package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(YavimayaAncients.class)
class YavimayaAncientsTest extends BaseCardTest {

    private Permanent addAncients() {
        return addCreatureReady(player1, new YavimayaAncients());
    }

    @Test
    @DisplayName("Ability gives +1/-2 until end of turn")
    void abilityBoosts() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ancients.getEffectivePower()).isEqualTo(3);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        Permanent ancients = harness.addToBattlefieldAndReturn(player1, new YavimayaAncients());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ancients.getEffectivePower()).isEqualTo(3);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating four times kills it via lethal toughness reduction")
    void repeatedActivationsKillIt() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.clearPriorityPassed();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ancients);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(ancients.getCard());
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ancients.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ancients.getPowerModifier()).isEqualTo(0);
        assertThat(ancients.getToughnessModifier()).isEqualTo(0);
    }
}
