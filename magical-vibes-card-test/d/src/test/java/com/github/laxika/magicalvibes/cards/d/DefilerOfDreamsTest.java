package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Omniscience;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Defiler of Dreams")
@CardUsed({DefilerOfDreams.class, CloudfinRaptor.class, Forest.class, GrizzlyBears.class, Unsummon.class, Omniscience.class})
class DefilerOfDreamsTest extends BaseCardTest {

    @Test
    void mayPayLifeWhenCastingWithoutPayingManaCost() {
        addCreatureReady(player1, new DefilerOfDreams());
        harness.addToBattlefield(player1, new Omniscience());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new CloudfinRaptor()));

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Cloudfin Raptor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("paying 2 life reduces a blue permanent spell by {U} and draws a card")
    void paysLifeForBluePermanentSpell() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.setLife(player1, 20);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("paying the reduced blue mana cost leaves life unchanged and draws a card")
    void paysReducedManaForBluePermanentSpell() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("non-blue permanents and blue nonpermanents do not trigger or receive the reduction")
    void ignoresNonMatchingSpells() {
        addCreatureReady(player1, new DefilerOfDreams());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);

        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, gd.playerBattlefields.get(player2.getId()).getFirst().getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    @DisplayName("Declining both Defilers' additional costs pays the full blue mana cost")
    void twoDefilersDoNotReduceCostWhenLifeIsNotPaid() {
        addCreatureReady(player1, new DefilerOfDreams());
        addCreatureReady(player1, new DefilerOfDreams());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new DefilerOfDreams()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Replacing two blue mana with two Defilers requires four life")
    void twoDefilersRequireTwoSeparateLifePayments() {
        addCreatureReady(player1, new DefilerOfDreams());
        addCreatureReady(player1, new DefilerOfDreams());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new DefilerOfDreams()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Defiler does not reduce its own cost or trigger for its own cast")
    void doesNotApplyBeforeEnteringBattlefield() {
        Forest undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player1, List.of(new DefilerOfDreams()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("An opponent's Defiler neither reduces your spell nor draws for it")
    void doesNotApplyToOpponentsSpells() {
        addCreatureReady(player2, new DefilerOfDreams());
        Forest undrawn = new Forest();
        harness.setLibrary(player2, List.of(undrawn));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DefilerOfDreams()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("Casting without paying mana does not require life or blue mana for Defiler")
    void mayDeclineLifePaymentWhenCastingWithoutManaCost() {
        addCreatureReady(player1, new DefilerOfDreams());
        harness.addToBattlefield(player1, new Omniscience());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new DefilerOfDreams()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }
}
