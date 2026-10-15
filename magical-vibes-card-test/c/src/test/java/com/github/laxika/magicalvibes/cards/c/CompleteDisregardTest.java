package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.r.RetreatToHagra;
import com.github.laxika.magicalvibes.cards.s.SwellOfGrowth;
import com.github.laxika.magicalvibes.cards.v.VestigeOfEmrakul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompleteDisregard.class, CoralhelmGuide.class, VestigeOfEmrakul.class, BroodhunterWurm.class, RetreatToHagra.class, SwellOfGrowth.class})
class CompleteDisregardTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a targeted creature with power 3 or less")
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        castCompleteDisregard(target);

        harness.assertNotOnBattlefield(player2, "Coralhelm Guide");
        harness.assertNotInGraveyard(player2, "Coralhelm Guide");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Coralhelm Guide"));
    }

    @Test
    @DisplayName("Can target a creature with exactly power 3")
    void canTargetPowerThreeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VestigeOfEmrakul());
        prepareCompleteDisregard();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        prepareCompleteDisregard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RetreatToHagra());
        prepareCompleteDisregard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Exiles a colorless creature with exactly three power")
    void exilesCreatureAtPowerLimit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VestigeOfEmrakul());

        castCompleteDisregard(target);

        harness.assertNotOnBattlefield(player2, "Vestige of Emrakul");
        harness.assertNotInGraveyard(player2, "Vestige of Emrakul");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can exile its controller's own creature")
    void exilesOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());

        castCompleteDisregard(target);

        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
        harness.assertNotInGraveyard(player1, "Coralhelm Guide");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature whose effective power has grown above three")
    void cannotTargetBoostedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player2, List.of(new SwellOfGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);
        prepareCompleteDisregard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Does not exile a creature that grows above three power in response")
    void rechecksPowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        prepareCompleteDisregard();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new SwellOfGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Coralhelm Guide");
        harness.assertInGraveyard(player1, "Complete Disregard");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(target.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    private void castCompleteDisregard(Permanent target) {
        prepareCompleteDisregard();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareCompleteDisregard() {
        harness.setHand(player1, List.of(new CompleteDisregard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
