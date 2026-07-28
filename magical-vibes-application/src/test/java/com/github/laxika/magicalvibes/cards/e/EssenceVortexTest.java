package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EssenceVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays life equal to the creature's toughness and it survives")
    void controllerPaysToughnessCreatureSurvives() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        // Grizzly Bears is 2/2 — two life.
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Controller declines and the creature is destroyed")
    void controllerDeclinesCreatureDestroyed() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A controller with too little life can't pay and the creature is destroyed automatically")
    void cannotPayDestroysAutomatically() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 1);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature destroyed this way can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);
        harness.setLife(player2, 20);
        castVortexOn(skeletons);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Boosted toughness raises the life the controller must pay")
    void boostedToughnessRaisesLifeCost() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setToughnessModifier(3);
        harness.setLife(player2, 20);
        castVortexOn(target);

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EssenceVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castVortexOn(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
