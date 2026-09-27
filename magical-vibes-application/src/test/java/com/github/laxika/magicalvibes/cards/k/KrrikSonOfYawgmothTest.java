package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.z.ZofShade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrrikSonOfYawgmoth.class, FeralShadow.class, DarkRitual.class, ZofShade.class})
class KrrikSonOfYawgmothTest extends BaseCardTest {

    private Permanent addKrrik() {
        harness.addToBattlefield(player1, new KrrikSonOfYawgmoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    void mayPayTwoLifeForEachBlackManaInAspellCost() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new DarkRitual()));

        harness.castInstant(player1, 0);

        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blackManaIsUsedBeforePayingLife() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayPayLifeForBlackActivatedAbilityCosts() {
        addKrrik();
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new ZofShade());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
    }
}
