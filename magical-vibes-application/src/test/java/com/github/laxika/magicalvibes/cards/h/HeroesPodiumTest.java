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
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroesPodiumTest extends BaseCardTest {

    @Test
    @DisplayName("Each legendary creature gets +1/+1 for each other legendary creature you control")
    void boostsLegendaryCreaturesByOtherControlledLegends() {
        Permanent first = addPermanent(player1, new TsaboTavoc());
        Permanent second = addPermanent(player1, new EmpressGalina());
        Permanent nonlegendary = addPermanent(player1, new GrizzlyBears());
        int firstBasePower = gqs.getEffectivePower(gd, first);
        int firstBaseToughness = gqs.getEffectiveToughness(gd, first);
        int secondBasePower = gqs.getEffectivePower(gd, second);
        int nonlegendaryBasePower = gqs.getEffectivePower(gd, nonlegendary);

        addPermanent(player1, new HeroesPodium());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(firstBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(secondBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, nonlegendary)).isEqualTo(nonlegendaryBasePower);
    }

    @Test
    @DisplayName("The source receives the bonus if it is also made into a legendary creature")
    void animatedSourceReceivesBonus() {
        Permanent podium = addPermanent(player1, new HeroesPodium());
        addPermanent(player1, new MarchOfTheMachines());
        int podiumPowerBeforeAnotherLegend = gqs.getEffectivePower(gd, podium);
        Permanent otherLegend = new Permanent(new TsaboTavoc());
        int otherLegendPower = otherLegend.getEffectivePower();
        gd.playerBattlefields.get(player1.getId()).add(otherLegend);

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

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
