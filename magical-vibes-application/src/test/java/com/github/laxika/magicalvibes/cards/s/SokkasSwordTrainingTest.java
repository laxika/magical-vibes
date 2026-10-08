package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SokkasSwordTraining.class, FountainOfYouth.class, GrizzlyBears.class})
class SokkasSwordTrainingTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature and creates a Clue token")
    void boostsTargetCreatureAndCreatesClue() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("Targeting an opponent's creature still creates the Clue for the caster")
    void targetingOpponentsCreatureCreatesClueForCaster() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not create a Clue when its only target leaves the battlefield")
    void doesNotCreateClueWhenTargetLeavesBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        addMana();

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerGraveyards.get(player1.getId()).add(bear.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sokka's Sword Training");
    }

    @Test
    @DisplayName("The Clue is sacrificed as a cost and draws a card on resolution")
    void clueCanBeSacrificedToDrawCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SokkasSwordTraining()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
