package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CasalLurkwoodPathfinder.class, CasalPathbreakerOwlbear.class, Forest.class, GrizzlyBears.class})
class CasalLurkwoodPathfinderTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAForestTapped() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.castFromHand(player1, new CasalLurkwoodPathfinder(), "{3}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void attackingMayPayToTransformAndBuffOtherLegendaryCreatures() {
        Permanent casal = addReady(player1, new CasalLurkwoodPathfinder());
        Permanent otherLegendary = addReady(player1, legendaryCreature());
        Permanent nonLegendary = addReady(player1, new GrizzlyBears());
        int otherLegendaryPower = gqs.getEffectivePower(gd, otherLegendary);
        int otherLegendaryToughness = gqs.getEffectiveToughness(gd, otherLegendary);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, casal)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherLegendary)).isEqualTo(otherLegendaryPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherLegendary)).isEqualTo(otherLegendaryToughness + 2);
        assertThat(gqs.hasKeyword(gd, otherLegendary, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonLegendary, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void backFaceTransformsAtItsControllersUpkeep() {
        Permanent casal = addReady(player1, new CasalLurkwoodPathfinder());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isFalse();
        assertThat(casal.getCard().getName()).isEqualTo("Casal, Lurkwood Pathfinder");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Card legendaryCreature() {
        Card card = new Card();
        card.setName("Legendary Creature");
        card.setType(CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
