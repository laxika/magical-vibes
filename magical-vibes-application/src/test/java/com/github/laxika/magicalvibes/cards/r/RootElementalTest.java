package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.d.DecreeOfPain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootElemental.class, ElvishAberration.class, DecreeOfPain.class})
class RootElementalTest extends BaseCardTest {
    @Test
    void castingFaceUpDoesNotTriggerTheHandToBattlefieldAbility() {
        ElvishAberration aberration = new ElvishAberration();
        harness.setHand(player1, List.of(new RootElemental(), aberration));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Root Elemental");
        harness.assertNotOnBattlefield(player1, "Elvish Aberration");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aberration);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceUpAbilityPutsOnlyTheChosenCreatureOntoBattlefield() {
        ElvishAberration first = new ElvishAberration();
        ElvishAberration second = new ElvishAberration();
        harness.setHand(player1, List.of(new RootElemental(), first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(findPermanent(player1, "Elvish Aberration").getCard()).isSameAs(second);
        assertThat(countPermanents(player1, "Elvish Aberration")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void turningFaceUpMayPutCreatureFromHandOntoBattlefield() {
        ElvishAberration aberration = new ElvishAberration();
        harness.setHand(player1, List.of(new RootElemental(), aberration));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Elvish Aberration");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void puttingCreatureOntoBattlefieldDoesNotRequireItsManaCost() {
        ElvishAberration aberration = new ElvishAberration();
        harness.setHand(player1, List.of(new RootElemental(), aberration));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Elvish Aberration");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningFaceUpAbilityLeavesHandUnchanged() {
        ElvishAberration aberration = new ElvishAberration();
        harness.setHand(player1, List.of(new RootElemental(), aberration));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Elvish Aberration");
        harness.assertNotOnBattlefield(player1, "Elvish Aberration");
    }

    @Test
    void faceUpAbilityDoesNotUseAnOpponentsHand() {
        harness.setHand(player1, List.of(new RootElemental()));
        harness.setHand(player2, List.of(new ElvishAberration()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Elvish Aberration");
    }

    @Test
    void onlyCreatureCardsAreOffered() {
        harness.setHand(player1, List.of(new RootElemental(), new DecreeOfPain(), new ElvishAberration()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int rootElementalIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.turnFaceUp(player1, rootElementalIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }
}
