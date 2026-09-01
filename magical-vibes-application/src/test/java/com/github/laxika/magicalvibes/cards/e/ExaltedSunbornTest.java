package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BrazenFreebooter;
import com.github.laxika.magicalvibes.cards.h.HangedExecutioner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExaltedSunborn.class, HangedExecutioner.class, BrazenFreebooter.class})
class ExaltedSunbornTest extends BaseCardTest {

    @Test
    void doublesCreatureTokensCreatedUnderItsControllersControl() {
        harness.addToBattlefield(player1, new ExaltedSunborn());
        harness.setHand(player1, List.of(new HangedExecutioner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void doublesNoncreatureTokensCreatedUnderItsControllersControl() {
        harness.addToBattlefield(player1, new ExaltedSunborn());
        harness.setHand(player1, List.of(new BrazenFreebooter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void warpExilesTheCreatureAtTheNextEndStep() {
        ExaltedSunborn sunborn = new ExaltedSunborn();
        harness.setHand(player1, List.of(sunborn));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(sunborn.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(sunborn.getId())).isNotNull();
    }
}
