package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({Monstrify.class, GrizzlyBears.class, Mountain.class})
class MonstrifyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +4/+4 until end of turn")
    void boostsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Monstrify()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Monstrify()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(bear.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Retrace recasts from the graveyard by discarding a land, returning to graveyard")
    void retraceDiscardsLandAndBoosts() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Monstrify()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castRetrace(player1, 0, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Monstrify");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without boosting other creatures")
    void boostsOpponentsCreatureOnly() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Monstrify()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(opponent.getId()));
        harness.passBothPriorities();

        assertThat(opponent.getPowerModifier()).isEqualTo(4);
        assertThat(opponent.getToughnessModifier()).isEqualTo(4);
        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Retrace can be used repeatedly and each boost adds to the previous one")
    void retracesRepeatedly() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Monstrify monstrify = new Monstrify();
        harness.setGraveyard(player1, List.of(monstrify));
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castRetrace(player1, 0, 0, bear.getId());
        harness.passBothPriorities();
        int graveyardIndex = harness.getGameData().playerGraveyards.get(player1.getId()).indexOf(monstrify);
        assertThat(graveyardIndex).isNotNegative();
        harness.castRetrace(player1, graveyardIndex, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(8);
        assertThat(bear.getToughnessModifier()).isEqualTo(8);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .contains(monstrify).hasSize(3);
    }

    @Test
    @DisplayName("Retrace rejects discarding a nonland card")
    void retraceRequiresLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Monstrify()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Monstrify");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace still requires the full mana cost and does not discard on rejection")
    void retraceRequiresMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Monstrify()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Monstrify");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace does not permit casting Monstrify during the end step")
    void retraceRequiresSorceryTiming() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Monstrify()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Monstrify");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
