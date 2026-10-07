package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.e.EsperSentinel;
import com.github.laxika.magicalvibes.cards.i.IcefallRegent;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.NornsAnnex;
import com.github.laxika.magicalvibes.cards.s.SmotheringTithe;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaxTaker.class, TitheTaker.class, DarkRitual.class, EsperSentinel.class,
        MindStone.class, NornsAnnex.class, SmotheringTithe.class, IcefallRegent.class, UltimatePrice.class})
class TaxTakerTest extends BaseCardTest {

    @Test
    void createsTreasuresWhenAnOpponentPaysAConditionalSpellTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void createsTreasuresWhenAnOpponentPaysEsperSentinelTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void createsTreasuresEqualToTheCombinedSpellTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTreasuresWhenOpponentDeclinesToPay() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new EsperSentinel());
        harness.setLibrary(player1, List.of(new DarkRitual()));
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.assertInHand(player1, "Dark Ritual");
    }

    @Test
    void doesNotCreateTreasuresForAnOpponentsTaxSource() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player2, new EsperSentinel());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void createsTwoTreasuresWhenOpponentPaysSmotheringTithe() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new SmotheringTithe());
        harness.setLibrary(player2, List.of(new DarkRitual()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void createsTreasureWhenOpponentPaysManaToAttackThroughNornsAnnex() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new NornsAnnex());
        addCreatureReady(player2, new TitheTaker());
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void doesNotCreateTreasureWhenOpponentPaysLifeToAttackThroughNornsAnnex() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new NornsAnnex());
        addCreatureReady(player2, new TitheTaker());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void createsTreasuresWhenOpponentPaysIcefallRegentsTargetingTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        var regent = harness.addToBattlefieldAndReturn(player1, new IcefallRegent());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, regent.getId());

        harness.assertInGraveyard(player1, "Icefall Regent");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
