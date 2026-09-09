package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Xorn.class, ForswornPaladin.class, BladeSplicer.class})
class XornTest extends BaseCardTest {

    @Test
    void addsOneTreasureToTreasureCreation() {
        harness.addToBattlefield(player1, new Xorn());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void eachXornAddsOneTreasure() {
        harness.addToBattlefield(player1, new Xorn());
        harness.addToBattlefield(player1, new Xorn());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    void doesNotAddToCreatureTokens() {
        harness.addToBattlefield(player1, new Xorn());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
    }
    @Test
    @CardUsed(OjerTaqDeepestFoundation.class)
    void creatureOnlyMultiplierDoesNotMultiplyAdditionalTreasures() {
        harness.addToBattlefield(player1, new Xorn());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @CardUsed(OjerTaqDeepestFoundation.class)
    void creatureOnlyMultiplierStillAppliesAlongsideXorn() {
        harness.addToBattlefield(player1, new Xorn());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3);
    }
}
