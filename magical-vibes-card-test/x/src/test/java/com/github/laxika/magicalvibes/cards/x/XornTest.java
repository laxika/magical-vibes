package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.d.DeadlyDispute;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.cards.u.UnexpectedWindfall;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Xorn.class, ForswornPaladin.class, BladeSplicer.class, OjerTaqDeepestFoundation.class,
        UnexpectedWindfall.class, DeadlyDispute.class, DoublingSeason.class})
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
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
    }
    @Test
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
    void creatureOnlyMultiplierStillAppliesAlongsideXorn() {
        harness.addToBattlefield(player1, new Xorn());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(3);
    }

    @Test
    void addsOnlyOneTreasureToAnEventCreatingTwoTreasures() {
        harness.addToBattlefield(player1, new Xorn());
        harness.setHand(player1, List.of(new UnexpectedWindfall(), new Xorn()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    void doesNotIncreaseOpponentsTreasureCreation() {
        harness.addToBattlefield(player1, new Xorn());
        addCreatureReady(player2, new ForswornPaladin());
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    void doesNotApplyWhenSacrificedToPayForTheTreasureCreatingSpell() {
        var xorn = harness.addToBattlefieldAndReturn(player1, new Xorn());
        harness.setHand(player1, List.of(new DeadlyDispute()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, xorn.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Xorn");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void allowsChoosingOrderWithATokenDoubler() {
        harness.addToBattlefield(player1, new Xorn());
        harness.addToBattlefield(player1, new DoublingSeason());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }
}
