package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SorinGrimNemesis;
import com.github.laxika.magicalvibes.cards.t.TeferisTalent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KasminaEnigmaSage.class, DarkRitual.class, Divination.class, GiantGrowth.class,
        Shock.class, SorinGrimNemesis.class, TeferisTalent.class})
class KasminaEnigmaSageTest extends BaseCardTest {

    @Test
    @DisplayName("+2 scries 1")
    void plusTwoScriesOne() {
        Permanent kasmina = addReadyPlaneswalker(new KasminaEnigmaSage(), 3);
        harness.setLibrary(player1, List.of(new GiantGrowth()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kasmina.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("-X creates a Fractal with X +1/+1 counters")
    void minusXCreatesFractal() {
        addReadyPlaneswalker(new KasminaEnigmaSage(), 5);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Fractal"))
                .toList();
        assertThat(fractals).hasSize(1);
        assertThat(fractals.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kasmina grants her loyalty abilities to another planeswalker")
    void grantsAbilitiesToAnotherPlaneswalker() {
        addReadyPlaneswalker(new KasminaEnigmaSage(), 5);
        Permanent sorin = addReadyPlaneswalker(new SorinGrimNemesis(), 5);
        harness.setLibrary(player1, List.of(new GiantGrowth()));

        harness.activateAbility(player1, 1, 3, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The granted ultimate uses the activating planeswalker's color")
    void grantedUltimateUsesActivatingPlaneswalkersColor() {
        addReadyPlaneswalker(new KasminaEnigmaSage(), 5);
        Permanent sorin = addReadyPlaneswalker(new SorinGrimNemesis(), 8);
        harness.setLibrary(player1, List.of(new Shock(), new Divination(), new DarkRitual()));

        harness.activateAbility(player1, 1, 5, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst()).isInstanceOf(DarkRitual.class);

        int blackManaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK))
                .isEqualTo(blackManaBefore + 3);
    }

    @Test
    @DisplayName("Zero X creates a Fractal that dies with zero toughness")
    void zeroXFractalDies() {
        Permanent kasmina = addReadyPlaneswalker(new KasminaEnigmaSage(), 3);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(kasmina.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Spending all loyalty still creates the Fractal after Kasmina dies")
    void spendingAllLoyaltyStillCreatesFractal() {
        addReadyPlaneswalker(new KasminaEnigmaSage(), 3);

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kasmina, Enigma Sage");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(fractal ->
                        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
    }

    @Test
    @DisplayName("The ultimate can exile a blue sorcery and decline to cast it")
    void ultimateCanDeclineFreeCast() {
        addReadyPlaneswalker(new KasminaEnigmaSage(), 8);
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(divination, new Shock(), new DarkRitual()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(divination);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(divination);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(divination);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(divination);
    }

    @Test
    @DisplayName("Kasmina shares loyalty abilities gained from an Aura")
    void sharesGainedLoyaltyAbility() {
        Permanent kasmina = addReadyPlaneswalker(new KasminaEnigmaSage(), 3);
        Permanent sorin = addReadyPlaneswalker(new SorinGrimNemesis(), 13);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(kasmina.getId());

        harness.activateAbility(player1, 1, 6, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(kasmina.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.emblems).hasSize(1);
    }

    private Permanent addReadyPlaneswalker(Card card, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
