package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulRenewal.class, AirElemental.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class FoulRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the creature card and gives the target creature -X/-X based on its toughness")
    void returnsCreatureAndShrinksTarget() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        harness.castInstant(player1, 0, graveyardCreature.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Still returns the card when the creature target becomes illegal")
    void creatureTargetBecomesIllegalStillReturnsCard() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        harness.castInstant(player1, 0, graveyardCreature.getId(), target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Does not shrink the creature when the graveyard target becomes illegal")
    void graveyardTargetBecomesIllegalLeavesCreatureUnaffected() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        harness.castInstant(player1, 0, graveyardCreature.getId(), target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOff() {
        Card graveyardCreature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        harness.castInstant(player1, 0, graveyardCreature.getId(), target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card graveyardLand = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(graveyardLand));
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, graveyardLand.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FoulRenewal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, graveyardCreature.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
