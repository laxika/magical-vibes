package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TuneUpTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card to the battlefield")
    void returnsArtifactFromGraveyard() {
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new TuneUp()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Spellbook");
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Returns a Vehicle as a permanent artifact creature")
    void animatesReturnedVehiclePermanently() {
        DuskLegionDreadnought vehicle = new DuskLegionDreadnought();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.setHand(player1, List.of(new TuneUp()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, vehicle.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Dusk Legion Dreadnought");
        assertThat(gqs.isCreature(gd, returned)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        returned = findPermanent(player1, "Dusk Legion Dreadnought");
        assertThat(gqs.isCreature(gameData, returned)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in the graveyard")
    void cannotTargetNonArtifactCard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TuneUp()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
