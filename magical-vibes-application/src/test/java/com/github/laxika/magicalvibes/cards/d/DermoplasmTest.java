package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.c.ChromeshellCrab;
import com.github.laxika.magicalvibes.cards.u.UndercoverCrocodelf;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dermoplasm.class, ChromeshellCrab.class, AvenEnvoy.class})
class DermoplasmTest extends BaseCardTest {

    @Test
    void putsCreatureWithMorphOntoBattlefieldAndReturnsDermoplasm() {
        harness.setHand(player1, List.of(new Dermoplasm(), new AvenEnvoy(), new ChromeshellCrab()));
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
        harness.assertInHand(player1, "Aven Envoy");
    }

    @Test
    void doesNotOfferCreatureWithoutMorphAbility() {
        harness.setHand(player1, List.of(new Dermoplasm(), new AvenEnvoy()));
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Aven Envoy");
        assertThat(dermoplasm.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Dermoplasm");
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

    @Test
    @CardUsed(ZoeticCavern.class)
    void doesNotOfferNonCreatureWithMorphAbility() {
        harness.setHand(player1, List.of(new Dermoplasm(), new ZoeticCavern()));
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Zoetic Cavern");
        assertThat(dermoplasm.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Dermoplasm");
    }

    @Test
    @CardUsed(UndercoverCrocodelf.class)
    void doesNotOfferCreatureWithDisguiseInsteadOfMorph() {
        harness.setHand(player1, List.of(new Dermoplasm(), new UndercoverCrocodelf()));
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Dermoplasm");
        harness.assertInHand(player1, "Undercover Crocodelf");
    }
}
