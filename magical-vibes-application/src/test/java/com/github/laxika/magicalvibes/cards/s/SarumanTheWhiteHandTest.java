package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GoblinCratermaker;
import com.github.laxika.magicalvibes.cards.g.GoblinWardriver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarumanTheWhiteHand.class, Divination.class, GoblinWardriver.class, GrizzlyBears.class,
        GoForTheThroat.class, GoblinCratermaker.class, BoggartShenanigans.class, Earthquake.class})
class SarumanTheWhiteHandTest extends BaseCardTest {

    @Test
    void amassesOrcsEqualToTheManaValueOfEachNoncreatureSpell() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, army, Keyword.WARD)).isTrue();
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Orc Army"));
    }

    @Test
    void grantsWardToControlledGoblinsAndOrcsOnly() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent goblin = addCreatureReady(player1, new GoblinWardriver());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.WARD)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.WARD)).isFalse();
    }

    @Test
    void subsequentNoncreatureSpellsGrowTheExistingArmy() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent firstTarget = addCreatureReady(player2, new GoblinCratermaker());
        Permanent secondTarget = addCreatureReady(player2, new GoblinCratermaker());
        harness.setHand(player1, List.of(new GoForTheThroat(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, firstTarget.getId());
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castInstant(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Orc Army")).isEqualTo(1);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAmass() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent target = addCreatureReady(player1, new GoblinCratermaker());
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());

        assertThat(countPermanents(player1, "Orc Army")).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Orc Army")).isZero();
        harness.assertOnBattlefield(player1, "Goblin Cratermaker");
        harness.assertInGraveyard(player2, "Go for the Throat");
    }

    @Test
    void payingTwoManaForGrantedWardLetsTheSpellResolve() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent goblin = addCreatureReady(player1, new GoblinCratermaker());
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, goblin.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Goblin Cratermaker");
    }

    @Test
    void anAmassedOrcArmyCountersAnUnpaidOpposingSpell() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent target = addCreatureReady(player2, new GoblinCratermaker());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Orc Army");
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, army.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Orc Army");
        harness.assertInGraveyard(player2, "Go for the Throat");
    }

    @Test
    void grantsWardToNoncreatureGoblinPermanents() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        assertThat(gqs.hasKeyword(gd, enchantment, Keyword.WARD)).isTrue();
    }

    @Test
    void doesNotGrantWardToOpponentsGoblins() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent goblin = addCreatureReady(player2, new GoblinCratermaker());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.WARD)).isFalse();
    }

    @Test
    void amassIncludesTheChosenXInTheSpellsManaValue() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Orc Army");
    }

    @Test
    void amassStillResolvesAfterSarumanLeavesTheBattlefield() {
        addCreatureReady(player1, new SarumanTheWhiteHand());
        Permanent target = addCreatureReady(player2, new GoblinCratermaker());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, findPermanent(player1, "Saruman, the White Hand").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Saruman, the White Hand");
        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.WARD)).isFalse();
    }
}
