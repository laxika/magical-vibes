package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroesPodium.class, EmpressGalina.class, FountainOfYouth.class, GrizzlyBears.class,
        MarchOfTheMachines.class, TsaboTavoc.class})
class HeroesPodiumTest extends BaseCardTest {

    @Test
    @DisplayName("Each legendary creature gets +1/+1 for each other legendary creature you control")
    void boostsLegendaryCreaturesByOtherControlledLegends() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmpressGalina());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int firstBasePower = gqs.getEffectivePower(gd, first);
        int firstBaseToughness = gqs.getEffectiveToughness(gd, first);
        int secondBasePower = gqs.getEffectivePower(gd, second);
        int nonlegendaryBasePower = gqs.getEffectivePower(gd, nonlegendary);

        harness.addToBattlefield(player1, new HeroesPodium());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(firstBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(secondBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, nonlegendary)).isEqualTo(nonlegendaryBasePower);
    }

    @Test
    @DisplayName("The source receives the bonus if it is also made into a legendary creature")
    void animatedSourceReceivesBonus() {
        Permanent podium = harness.addToBattlefieldAndReturn(player1, new HeroesPodium());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        int podiumPowerBeforeAnotherLegend = gqs.getEffectivePower(gd, podium);
        Permanent otherLegend = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        int otherLegendPower = otherLegend.getEffectivePower();

        assertThat(gqs.getEffectivePower(gd, podium)).isEqualTo(podiumPowerBeforeAnotherLegend + 1);
        assertThat(gqs.getEffectivePower(gd, otherLegend)).isEqualTo(otherLegendPower + 1);
    }

    @Test
    @DisplayName("The activated ability looks at X cards and offers a legendary creature")
    void searchesTopXForLegendaryCreature() {
        Permanent podium = harness.addToBattlefieldAndReturn(player1, new HeroesPodium());
        podium.setSummoningSick(false);
        Card legend = new TsaboTavoc();
        Card nonlegendary = new GrizzlyBears();
        Card artifact = new FountainOfYouth();
        Card belowTopX = new GrizzlyBears();
        harness.setLibrary(player1, List.of(legend, nonlegendary, artifact, belowTopX));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int podiumIndex = gd.playerBattlefields.get(player1.getId()).indexOf(podium);
        harness.activateAbility(player1, podiumIndex, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(legend, nonlegendary, artifact);
        assertThat(choice.validCardIds()).containsExactly(legend.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(legend.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(legend);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonlegendary, artifact, belowTopX);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability puts all ineligible cards on the bottom")
    void noLegendaryCreatureLeavesLibraryCardsOnBottom() {
        Permanent podium = harness.addToBattlefieldAndReturn(player1, new HeroesPodium());
        podium.setSummoningSick(false);
        Card nonlegendary = new GrizzlyBears();
        Card artifact = new FountainOfYouth();
        harness.setLibrary(player1, List.of(nonlegendary, artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int podiumIndex = gd.playerBattlefields.get(player1.getId()).indexOf(podium);
        harness.activateAbility(player1, podiumIndex, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonlegendary, artifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonlegendary, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void loneLegendDoesNotCountItselfArtifactsOrOpponentsLegends() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new EmpressGalina());
        int ownPower = gqs.getEffectivePower(gd, own);
        int opposingPower = gqs.getEffectivePower(gd, opposing);
        harness.addToBattlefield(player1, new HeroesPodium());

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(ownPower);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(opposingPower);
    }

    @Test
    void mayDeclineLegendAndBottomOnlyTheLookedAtCards() {
        harness.addToBattlefield(player1, new HeroesPodium());
        Card legend = new TsaboTavoc();
        Card other = new GrizzlyBears();
        Card untouched = new FountainOfYouth();
        harness.setLibrary(player1, List.of(legend, other, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(legend, other);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(legend, other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXStillTapsButDoesNotLookAtAnyCards() {
        Permanent podium = harness.addToBattlefieldAndReturn(player1, new HeroesPodium());
        Card legend = new TsaboTavoc();
        harness.setLibrary(player1, List.of(legend));

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(podium.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(legend);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(legend);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void looksAtAvailableCardsWhenXExceedsLibraryAndSelectsOnlyOneLegend() {
        harness.addToBattlefield(player1, new HeroesPodium());
        Card first = new TsaboTavoc();
        Card second = new EmpressGalina();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }
}
