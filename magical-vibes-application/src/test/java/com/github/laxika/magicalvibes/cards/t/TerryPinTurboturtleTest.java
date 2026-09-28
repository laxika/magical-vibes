package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasiliskGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerryPinTurboturtle.class, BasiliskGate.class, GrizzlyBears.class})
class TerryPinTurboturtleTest extends BaseCardTest {

    @Test
    @DisplayName("Sorcery-speed activated abilities can be activated at instant speed")
    void removesSorcerySpeedRestrictionFromActivatedAbilities() {
        harness.addToBattlefield(player1, new TerryPinTurboturtle());
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
