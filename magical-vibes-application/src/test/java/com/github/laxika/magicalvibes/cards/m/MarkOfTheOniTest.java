package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.y.YukoraThePrisoner;
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

@CardUsed({MarkOfTheOni.class, GnarledMass.class, YukoraThePrisoner.class, MirrorGallery.class})
class MarkOfTheOniTest extends BaseCardTest {

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    private Permanent castOn(Permanent target) {
        harness.setHand(player1, List.of(new MarkOfTheOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("Resolving Mark of the Oni steals the enchanted creature")
    void stealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());

        Permanent aura = castOn(creature);

        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Sacrifices itself at end step when controller has no Demons")
    void sacrificesWithoutDemon() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        castOn(creature);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of the Oni");
        // Control reverts to the owner once the Aura is gone.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does not trigger while the controller has a Demon")
    void noTriggerWithDemon() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        castOn(creature);
        harness.addToBattlefield(player1, new YukoraThePrisoner());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("An enchanted Demon you now control keeps the Aura around")
    void enchantedDemonCounts() {
        Permanent enemyDemon = addCreatureReady(player2, new YukoraThePrisoner());
        castOn(enemyDemon);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("An opponent's Demon does not prevent the sacrifice")
    void opponentDemonDoesNotHelp() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        castOn(creature);
        harness.addToBattlefield(player2, new YukoraThePrisoner());

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("A Demon entering before the trigger resolves prevents the sacrifice")
    void demonEnteringBeforeResolutionPreventsSacrifice() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        castOn(creature);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new YukoraThePrisoner());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.setHand(player1, List.of(new MarkOfTheOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent artifact = findPermanent(player1, "Mirror Gallery");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
