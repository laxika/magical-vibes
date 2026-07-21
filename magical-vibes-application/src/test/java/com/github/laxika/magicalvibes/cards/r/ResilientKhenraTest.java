package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResilientKhenraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may give target creature +X/+X equal to Khenra's power")
    void etbBoostsTargetBySourcePower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndAcceptMay(bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(4); // 2 + 2
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may leaves the target unboosted")
    void decliningMaySkipsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndAcceptMay(bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie token whose ETB can give +4/+4")
    void eternalizeTokenEtbBoostsByFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve eternalize -> token ETB on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Resilient Khenra") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getManaCost()).isEmpty();
        assertThat(bears.getPowerModifier()).isEqualTo(4);
        assertThat(bears.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Resilient Khenra"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Resilient Khenra"));
    }

    private void castAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ResilientKhenra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
    }
}
