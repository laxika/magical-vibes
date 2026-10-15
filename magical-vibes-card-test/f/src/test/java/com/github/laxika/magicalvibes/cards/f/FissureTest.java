package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.d.Drowned;
import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.cards.w.WaterWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fissure.class, WaterWurm.class, MazeOfIth.class, BloodMoon.class, Drowned.class})
class FissureTest extends BaseCardTest {

    private void addFissureMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Resolving destroys target creature")
    void resolvesAndDestroysCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new WaterWurm());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Water Wurm");
        harness.assertInGraveyard(player2, "Water Wurm");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesAndDestroysLand() {
        var land = harness.addToBattlefieldAndReturn(player2, new MazeOfIth());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player2, "Maze of Ith");
        harness.assertInGraveyard(player2, "Maze of Ith");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonland permanent")
    void cannotTargetNonCreatureNonLandPermanent() {
        var enchantment = harness.addToBattlefieldAndReturn(player2, new BloodMoon());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature despite its regeneration shield")
    void destroysCreatureDespiteRegenerationShield() {
        var drowned = harness.addToBattlefieldAndReturn(player2, new Drowned());
        drowned.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        harness.castAndResolveInstant(player1, 0, drowned.getId());

        harness.assertNotOnBattlefield(player2, "Drowned");
        harness.assertInGraveyard(player2, "Drowned");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void destroysOwnCreature() {
        var creature = harness.addToBattlefieldAndReturn(player1, new WaterWurm());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Water Wurm");
        harness.assertInGraveyard(player1, "Water Wurm");
        harness.assertInGraveyard(player1, "Fissure");
    }

    @Test
    @DisplayName("Regeneration activated in response cannot save the target")
    void destroysCreatureRegeneratedInResponse() {
        var drowned = harness.addToBattlefieldAndReturn(player2, new Drowned());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, drowned.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drowned");
        assertThat(drowned.getRegenerationShield()).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drowned");
        harness.assertInGraveyard(player2, "Drowned");
        harness.assertInGraveyard(player1, "Fissure");
    }
}
