package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TianaAngelicMechanic.class, DuskLegionDreadnought.class, ArvadTheCursed.class, GrizzlyBears.class})
class TianaAngelicMechanicTest extends BaseCardTest {

    @Test
    void tianaPerpetuallyBoostsVehicleSheCrews() {
        addReady(player1, new TianaAngelicMechanic());
        Permanent vehicle = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
    }

    @Test
    void anotherLegendaryCreaturePerpetuallyBoostsVehicleItCrews() {
        Permanent tiana = addReady(player1, new TianaAngelicMechanic());
        tiana.tap();
        addReady(player1, new ArvadTheCursed());
        Permanent vehicle = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
    }

    @Test
    void nonLegendaryCreatureDoesNotTriggerTiana() {
        Permanent tiana = addReady(player1, new TianaAngelicMechanic());
        tiana.tap();
        addReady(player1, new GrizzlyBears());
        Permanent vehicle = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
