package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Imbraham;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KianneTest extends BaseCardTest {

    @Test
    void tapAbilityPutsLandIntoHand() {
        addReadyKianne();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.exiledCardsWithStudyCounters).doesNotContain(forest.getId());
    }

    @Test
    void tapAbilityExilesNonlandWithStudyCounter() {
        addReadyKianne();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.exiledCardsWithStudyCounters).contains(bears.getId());
    }

    @Test
    void tokenCountsDistinctNonlandStudyCounterManaValues() {
        addReadyKianne();
        com.github.laxika.magicalvibes.model.Card first = new Opt();
        com.github.laxika.magicalvibes.model.Card second = new GrizzlyBears();
        com.github.laxika.magicalvibes.model.Card duplicateValue = new GrizzlyBears();
        com.github.laxika.magicalvibes.model.Card land = new Forest();
        harness.setExile(player1, List.of(first, second, duplicateValue, land));
        gd.exiledCardsWithStudyCounters.addAll(List.of(
                first.getId(), second.getId(), duplicateValue.getId(), land.getId()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void imbrahamExilesXCardsAndReturnsChosenStudyCounterCard() {
        harness.setHand(player1, List.of(new Kianne()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent imbraham = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Imbraham)
                .findFirst()
                .orElseThrow();
        imbraham.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Opt(), new GrizzlyBears(), new Forest()));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(imbraham),
                0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        Opt chosen = (Opt) gd.getPlayerExiledCards(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.exiledCardsWithStudyCounters).contains(
                gd.getPlayerExiledCards(player1.getId()).stream()
                        .filter(card -> card instanceof GrizzlyBears)
                        .findFirst().orElseThrow().getId());
    }

    private Permanent addReadyKianne() {
        Permanent kianne = addCreatureReady(player1, new Kianne());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kianne;
    }

}
