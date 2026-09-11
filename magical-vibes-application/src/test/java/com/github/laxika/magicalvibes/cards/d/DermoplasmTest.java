package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChromeshellCrab;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dermoplasm.class, ChromeshellCrab.class, GrizzlyBears.class})
class DermoplasmTest extends BaseCardTest {

    @Test
    void putsCreatureWithMorphOntoBattlefieldAndReturnsDermoplasm() {
        harness.setHand(player1, List.of(new Dermoplasm(), new GrizzlyBears(), new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dermoplasm = findPermanent(player1, "Dermoplasm");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dermoplasm));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Chromeshell Crab");
        harness.assertInHand(player1, "Dermoplasm");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotOfferCreatureWithoutMorphAbility() {
        harness.setHand(player1, List.of(new Dermoplasm(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dermoplasm = findPermanent(player1, "Dermoplasm");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dermoplasm));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(dermoplasm.isFaceDown()).isFalse();
    }

    @Test
    void mayDeclinePuttingCreatureOntoBattlefield() {
        harness.setHand(player1, List.of(new Dermoplasm(), new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dermoplasm = findPermanent(player1, "Dermoplasm");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dermoplasm));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dermoplasm.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Dermoplasm");
        harness.assertInHand(player1, "Chromeshell Crab");
    }
}
