package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecroticWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -X/-X for the number of creature cards in the caster's graveyard")
    void givesMinusXMinusXForCreatureCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NecroticWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Centaur Courser");
    }

    @Test
    @DisplayName("Exiles a creature killed by the reduction instead of putting it into its graveyard")
    void exilesCreatureKilledByReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new NecroticWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(card -> card.card().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The exile replacement applies to a later death during the same turn")
    void exilesCreatureThatDiesLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NecroticWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(card -> card.card().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NecroticWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
