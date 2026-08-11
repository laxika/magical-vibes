package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThoughtweftChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 until end of turn")
    void boostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCharge(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws a card if a creature entered under your control this turn")
    void drawsAfterCreatureEnteredUnderYourControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new GrizzlyBears());
        harness.setHand(player1, List.of(new ThoughtweftCharge()));
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a creature entering under your control this turn")
    void doesNotDrawWithoutCreatureEntry() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThoughtweftCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw for a noncreature entry or an opponent's creature entry")
    void doesNotDrawForWrongEntry() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new FountainOfYouth());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new ArrayList<>())
                .add(new GrizzlyBears());
        harness.setHand(player1, List.of(new ThoughtweftCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new ThoughtweftCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player1, "Spellbook");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCharge(UUID targetId) {
        harness.setHand(player1, List.of(new ThoughtweftCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
