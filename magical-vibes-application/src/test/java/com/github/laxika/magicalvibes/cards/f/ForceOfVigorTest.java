package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfVigor.class, FountainOfYouth.class, PhyrexianArena.class, GrizzlyBears.class})
class ForceOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to two target artifacts and enchantments")
    void destroysTwoTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.setHand(player1, List.of(new ForceOfVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(artifact.getId(), enchantment.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("Can target no permanents")
    void canTargetNoPermanents() {
        harness.setHand(player1, List.of(new ForceOfVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast by exiling a green card during an opponent's turn")
    void castsWithAlternateCostDuringOpponentsTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        ForceOfVigor force = new ForceOfVigor();
        GrizzlyBears greenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(force, greenCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstantWithAlternateExileFromHand(player1, 0, artifact.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-artifact, non-enchantment permanent")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForceOfVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts and/or enchantments");
    }
}
