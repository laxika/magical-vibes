package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentKraj.class, OgreMenial.class})
class ExperimentKrajTest extends BaseCardTest {

    @Test
    @DisplayName("Gains an activated ability from each other creature with a +1/+1 counter")
    void gainsActivatedAbilityFromCounteredCreature() {
        Permanent kraj = addReady(player1, new ExperimentKraj());
        Permanent ogre = addReady(player2, new OgreMenial());
        ogre.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kraj)).isEqualTo(5);
    }

    @Test
    @DisplayName("Stops gaining an activated ability when the creature loses its +1/+1 counter")
    void stopsGainingAbilityWhenCounterIsRemoved() {
        addReady(player1, new ExperimentKraj());
        Permanent ogre = addReady(player2, new OgreMenial());
        ogre.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        ogre.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its tap ability puts a +1/+1 counter on a target creature")
    void putsCounterOnTargetCreature() {
        addReady(player1, new ExperimentKraj());
        Permanent ogre = addReady(player2, new OgreMenial());

        harness.activateAbility(player1, 0, 0, null, ogre.getId());
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays the copied activated ability with its required mana color")
    void copiedAbilityKeepsItsManaColor() {
        addReady(player1, new ExperimentKraj());
        Permanent ogre = addReady(player2, new OgreMenial());
        ogre.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
