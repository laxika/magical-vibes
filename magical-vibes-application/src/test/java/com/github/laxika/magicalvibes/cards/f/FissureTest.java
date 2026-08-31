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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fissure.class, WaterWurm.class, MazeOfIth.class, BloodMoon.class, Drowned.class})
class FissureTest extends BaseCardTest {

    private void addFissureMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Resolving destroys target creature")
    void resolvesAndDestroysCreature() {
        harness.addToBattlefield(player2, new WaterWurm());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        UUID targetId = harness.getPermanentId(player2, "Water Wurm");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Water Wurm");
        harness.assertInGraveyard(player2, "Water Wurm");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesAndDestroysLand() {
        harness.addToBattlefield(player2, new MazeOfIth());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        UUID targetId = harness.getPermanentId(player2, "Maze of Ith");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Maze of Ith");
        harness.assertInGraveyard(player2, "Maze of Ith");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonland permanent")
    void cannotTargetNonCreatureNonLandPermanent() {
        harness.addToBattlefield(player2, new BloodMoon());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana();

        UUID enchantmentId = harness.getPermanentId(player2, "Blood Moon");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantmentId))
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
}
