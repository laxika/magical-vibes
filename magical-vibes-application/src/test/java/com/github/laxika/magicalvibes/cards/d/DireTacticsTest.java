package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireTactics.class, EliteVanguard.class, Forest.class, GrizzlyBears.class})
class DireTacticsTest extends BaseCardTest {

    private void castDireTactics(Permanent target) {
        harness.setHand(player1, List.of(new DireTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles a creature and loses life equal to its toughness")
    void exilesCreatureAndLosesLife() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDireTactics(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().exiledCards).anyMatch(
                exiled -> exiled.card().getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Uses the creature's effective toughness")
    void usesEffectiveToughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setToughnessModifier(3);

        castDireTactics(target);

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not lose life while controlling a Human")
    void noLifeLossWhileControllingHuman() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new EliteVanguard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDireTactics(target);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Exiling the only Human causes the life loss")
    void exilingOnlyHumanCausesLifeLoss() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        castDireTactics(target);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DireTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
