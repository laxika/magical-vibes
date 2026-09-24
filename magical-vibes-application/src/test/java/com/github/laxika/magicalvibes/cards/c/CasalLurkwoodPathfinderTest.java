package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CasalLurkwoodPathfinder.class, CasalPathbreakerOwlbear.class, Forest.class, GrizzlyBears.class})
class CasalLurkwoodPathfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by putting a Forest from the library onto the battlefield tapped")
    void entersAndSearchesForForest() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new CasalLurkwoodPathfinder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        Permanent fetchedForest = findPermanent(player1, "Forest");
        assertThat(fetchedForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("May pay to transform when it attacks")
    void transformsAfterPayingForAttackTrigger() {
        Permanent casal = addReadyCasal();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(casal.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack payment does not transform it")
    void doesNotTransformWhenAttackPaymentIsDeclined() {
        Permanent casal = addReadyCasal();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(casal.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transformation boosts other legendary creatures and grants them trample")
    void transformationBoostsOtherLegendaryCreatures() {
        Permanent casal = addReadyCasal();
        Permanent legendaryBear = addCreatureReady(player1, legendaryBear());
        Permanent ordinaryBear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, legendaryBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, legendaryBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, legendaryBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ordinaryBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ordinaryBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ordinaryBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Transforms back at the beginning of its controller's upkeep")
    void transformsBackOnControllersUpkeep() {
        Permanent casal = addTransformedCasal(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not transform back on an opponent's upkeep")
    void doesNotTransformBackOnOpponentsUpkeep() {
        Permanent casal = addTransformedCasal(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isTrue();
    }

    private Permanent addReadyCasal() {
        return addCreatureReady(player1, new CasalLurkwoodPathfinder());
    }

    private Permanent addTransformedCasal(Player player) {
        CasalLurkwoodPathfinder card = new CasalLurkwoodPathfinder();
        Permanent casal = addCreatureReady(player, card);
        casal.setCard(card.getBackFaceCard());
        casal.setTransformed(true);
        return casal;
    }

    private GrizzlyBears legendaryBear() {
        GrizzlyBears card = new GrizzlyBears();
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }

}
