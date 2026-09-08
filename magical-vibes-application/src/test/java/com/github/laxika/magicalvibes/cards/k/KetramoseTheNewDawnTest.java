package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JourneyToNowhere;
import com.github.laxika.magicalvibes.cards.r.RelicOfProgenitus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KetramoseTheNewDawnTest extends BaseCardTest {

    @Test
    void cannotAttackWithFewerThanSevenCardsInExile() {
        harness.setExile(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithSevenCardsInExile() {
        harness.setExile(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());

        declareAttackers(player1, List.of(0));
    }

    @Test
    void drawsAndLosesLifeWhenCardIsExiledFromAGraveyardDuringYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertLife(player1, 19);
    }

    @Test
    void drawsAndLosesLifeWhenPermanentIsExiledDuringYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertLife(player1, 19);
    }
}
