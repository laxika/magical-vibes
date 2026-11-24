package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Imbraham;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.ArchwayCommons;
import com.github.laxika.magicalvibes.cards.c.ChargeThrough;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kianne.class, Imbraham.class, ArchwayCommons.class, ChargeThrough.class, ScurridColony.class})
class KianneTest extends BaseCardTest {

    @Test
    void tapAbilityPutsLandIntoHand() {
        addReadyKianne();
        ArchwayCommons forest = new ArchwayCommons();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Archway Commons");
        assertThat(gd.exiledCardsWithCounter(CounterType.STUDY).keySet()).doesNotContain(forest.getId());
    }

    @Test
    void tapAbilityExilesNonlandWithStudyCounter() {
        addReadyKianne();
        ScurridColony bears = new ScurridColony();
        harness.setLibrary(player1, List.of(bears));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.exiledCardsWithCounter(CounterType.STUDY).keySet()).contains(bears.getId());
    }

    @Test
    void tokenCountsDistinctNonlandStudyCounterManaValues() {
        addReadyKianne();
        com.github.laxika.magicalvibes.model.Card first = new ChargeThrough();
        com.github.laxika.magicalvibes.model.Card second = new ScurridColony();
        com.github.laxika.magicalvibes.model.Card duplicateValue = new ScurridColony();
        com.github.laxika.magicalvibes.model.Card land = new ArchwayCommons();
        harness.setExile(player1, List.of(first, second, duplicateValue, land));
        gd.addExiledCardCounters(first.getId(), CounterType.STUDY, 1);
        gd.addExiledCardCounters(second.getId(), CounterType.STUDY, 1);
        gd.addExiledCardCounters(duplicateValue.getId(), CounterType.STUDY, 1);
        gd.addExiledCardCounters(land.getId(), CounterType.STUDY, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, fractal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fractal)).isEqualTo(2);
    }

    @Test
    void imbrahamExilesXCardsAndReturnsChosenStudyCounterCard() {
        harness.setHand(player1, List.of(new Kianne()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent imbraham = findPermanent(player1, "Imbraham, Dean of Theory");
        imbraham.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new ChargeThrough(), new ScurridColony(), new ArchwayCommons()));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(imbraham),
                0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        ChargeThrough chosen = (ChargeThrough) gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.exiledCardsWithCounter(CounterType.STUDY).keySet()).contains(
                gd.getPlayerExiledCards(player1.getId()).stream()
                        .filter(card -> card instanceof ScurridColony)
                        .findFirst().orElseThrow().getId());
    }

    @Test
    void imbrahamCanReturnAnOlderStudyCardWithZeroX() {
        addCreatureReady(player1, new Imbraham());
        ArchwayCommons chosen = new ArchwayCommons();
        ChargeThrough remaining = new ChargeThrough();
        ScurridColony unmarked = new ScurridColony();
        harness.setExile(player1, List.of(chosen, remaining, unmarked));
        gd.addExiledCardCounters(chosen.getId(), CounterType.STUDY, 1);
        gd.addExiledCardCounters(remaining.getId(), CounterType.STUDY, 1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(remaining, unmarked);
        assertThat(gd.exiledCardsWithCounter(CounterType.STUDY).keySet()).contains(remaining.getId()).doesNotContain(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void imbrahamMayDeclineReturningAnyStudyCard() {
        addCreatureReady(player1, new Imbraham());
        harness.setHand(player1, List.of());
        ArchwayCommons land = new ArchwayCommons();
        ChargeThrough spell = new ChargeThrough();
        harness.setLibrary(player1, List.of(land, spell));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, 0, 4, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(land, spell);
        assertThat(gd.exiledCardsWithCounter(CounterType.STUDY).keySet()).contains(land.getId(), spell.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void fractalIgnoresOpponentsAndUnmarkedCards() {
        addReadyKianne();
        ChargeThrough marked = new ChargeThrough();
        ScurridColony unmarked = new ScurridColony();
        Kianne opponentsCard = new Kianne();
        harness.setExile(player1, List.of(marked, unmarked));
        harness.setExile(player2, List.of(opponentsCard));
        gd.addExiledCardCounters(marked.getId(), CounterType.STUDY, 1);
        gd.addExiledCardCounters(opponentsCard.getId(), CounterType.STUDY, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void fractalDiesWhenThereAreNoEligibleStudyCards() {
        addReadyKianne();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    void kianneDoesNothingWithAnEmptyLibrary() {
        addReadyKianne();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Permanent addReadyKianne() {
        Permanent kianne = addCreatureReady(player1, new Kianne());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kianne;
    }

}
