package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrillOfTheHunt.class, AshcoatBear.class, Plains.class})
class ThrillOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = bears.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = bears.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Flashback gives the target creature +1/+2 and exiles the spell")
    void flashbackBoostsAndExiles() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = bears.getId();
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Thrill of the Hunt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thrill of the Hunt"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can boost an opponent's creature")
    void boostsOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Thrill of the Hunt");
    }

    @Test
    @DisplayName("Casting normally then flashing back stacks both boosts until cleanup")
    void normalCastThenFlashbackStacksBoosts() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Thrill of the Hunt");
        harness.castFlashback(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Thrill of the Hunt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thrill of the Hunt"));

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Flashback cannot be paid with the normal green mana cost")
    void flashbackRequiresWhiteMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Thrill of the Hunt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target has left the battlefield")
    void flashbackExilesWhenTargetIsGone() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new ThrillOfTheHunt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        harness.assertNotInGraveyard(player1, "Thrill of the Hunt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thrill of the Hunt"));
    }
}
