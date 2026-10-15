package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NissaOfShadowedBoughs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SoulShatter.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class,
        CanopyBaloth.class, CliffhavenSellSword.class, NissaOfShadowedBoughs.class})
class SoulShatterTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices an eligible permanent with greatest mana value")
    void sacrificesGreatestManaValueCreatureOrPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        addReadyChandra(player2, 5);

        castSoulShatter();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }

    private void castSoulShatter() {
        harness.castFromHand(player1, new SoulShatter(), "{2}{B}");
        harness.passBothPriorities();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void opponentCanChooseCreatureWhenCreatureAndPlaneswalkerTie() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaOfShadowedBoughs());
        nissa.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new CliffhavenSellSword());

        castSoulShatter();
        harness.handleMultiplePermanentsChosen(player2, List.of(baloth.getId()));

        harness.assertInGraveyard(player2, "Canopy Baloth");
        harness.assertNotOnBattlefield(player2, "Canopy Baloth");
        harness.assertOnBattlefield(player2, "Nissa of Shadowed Boughs");
        harness.assertOnBattlefield(player2, "Cliffhaven Sell-Sword");
    }

    @Test
    void opponentCanChoosePlaneswalkerWhenCreatureAndPlaneswalkerTie() {
        harness.addToBattlefield(player2, new CanopyBaloth());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaOfShadowedBoughs());
        nissa.setCounterCount(CounterType.LOYALTY, 4);

        castSoulShatter();
        harness.handleMultiplePermanentsChosen(player2, List.of(nissa.getId()));

        harness.assertInGraveyard(player2, "Nissa of Shadowed Boughs");
        harness.assertNotOnBattlefield(player2, "Nissa of Shadowed Boughs");
        harness.assertOnBattlefield(player2, "Canopy Baloth");
    }

    @Test
    void sacrificesGreatestCreatureAndLeavesControllersPermanentsAlone() {
        harness.addToBattlefield(player1, new CanopyBaloth());
        harness.addToBattlefield(player2, new CanopyBaloth());
        harness.addToBattlefield(player2, new CliffhavenSellSword());

        castSoulShatter();

        harness.assertOnBattlefield(player1, "Canopy Baloth");
        harness.assertInGraveyard(player2, "Canopy Baloth");
        harness.assertNotOnBattlefield(player2, "Canopy Baloth");
        harness.assertOnBattlefield(player2, "Cliffhaven Sell-Sword");
    }

    @Test
    void resolvesWithNoOpposingPermanents() {
        harness.addToBattlefield(player1, new CliffhavenSellSword());

        castSoulShatter();

        harness.assertOnBattlefield(player1, "Cliffhaven Sell-Sword");
        harness.assertInGraveyard(player1, "Soul Shatter");
    }
}
