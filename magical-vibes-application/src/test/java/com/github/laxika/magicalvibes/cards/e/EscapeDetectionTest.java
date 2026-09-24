package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeDetection.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class EscapeDetectionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature and draws a card")
    void returnsTargetCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EscapeDetection()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Freerunning returns a blue creature as an alternate cost")
    void freerunningReturnsBlueCreatureAsCost() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markAssassinCombatDamage();
        harness.setHand(player1, List.of(new EscapeDetection()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(blueCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Freerunning rejects a nonblue creature as its cost")
    void freerunningRejectsNonblueCreature() {
        Permanent nonblueCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markAssassinCombatDamage();
        harness.setHand(player1, List.of(new EscapeDetection()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(nonblueCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Freerunning requires qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EscapeDetection()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(blueCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }
}
