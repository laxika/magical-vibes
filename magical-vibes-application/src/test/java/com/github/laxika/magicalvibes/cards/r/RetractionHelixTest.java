package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.t.TempleOfEnlightenment;
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

@CardUsed({RetractionHelix.class, SwordwiseCentaur.class, TempleOfEnlightenment.class, SpringleafDrum.class})
class RetractionHelixTest extends BaseCardTest {

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent grantAbilityToReadyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        creature.setSummoningSick(false);

        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    @Test
    @DisplayName("Target creature gains the ability to return a nonland permanent to its owner's hand")
    void grantedAbilityBouncesNonlandPermanent() {
        Permanent creature = grantAbilityToReadyCreature();
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        harness.activateAbility(player1, 0, null, bounceTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bounceTarget);
        harness.assertInHand(player2, "Swordwise Centaur");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability cannot target a land")
    void grantedAbilityCannotTargetLand() {
        grantAbilityToReadyCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfEnlightenment());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("The granted ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        Permanent creature = grantAbilityToReadyCreature();
        harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        endTurn();
        creature.setSummoningSick(false);

        UUID bounceTarget = harness.getPermanentId(player2, "Swordwise Centaur");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bounceTarget))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Retraction Helix cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SwordwiseCentaur());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfEnlightenment());

        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void grantedAbilityCanBounceArtifact() {
        grantAbilityToReadyCreature();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Springleaf Drum");
        harness.assertInHand(player2, "Springleaf Drum");
    }

    @Test
    void grantedAbilityCanBounceItsOwnSource() {
        Permanent creature = grantAbilityToReadyCreature();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Swordwise Centaur");
        harness.assertInHand(player1, "Swordwise Centaur");
    }

    @Test
    void grantedAbilityCannotBeUsedWhileSummoningSick() {
        Permanent creature = grantAbilityToReadyCreature();
        creature.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Swordwise Centaur");
    }

    @Test
    void opponentControlsAbilityGrantedToTheirCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        creature.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Springleaf Drum");
        harness.assertInHand(player1, "Springleaf Drum");
    }

    @Test
    void tappedCreatureGainsAbilityButMustUntapToUseIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        creature.setSummoningSick(false);
        creature.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        creature.setTapped(false);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Swordwise Centaur");
    }
}
