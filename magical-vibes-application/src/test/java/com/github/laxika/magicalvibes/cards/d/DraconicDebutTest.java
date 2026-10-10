package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChardalynDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicDebut.class, ChardalynDragon.class, GrizzlyBears.class, Forest.class})
class DraconicDebutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to any target")
    void dealsXDamageToAnyTarget() {
        harness.setHand(player1, List.of(new DraconicDebut()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Reduces the next Dragon creature spell by X and is consumed")
    void reducesNextDragonCreatureSpellOnly() {
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce a non-Dragon creature spell")
    void doesNotReduceNonDragonCreatureSpell() {
        harness.setHand(player1, List.of(new DraconicDebut(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DraconicDebut()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unused Dragon reduction survives until a later turn")
    void reductionSurvivesTurnCleanup() {
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casting a non-Dragon does not consume the Dragon reduction")
    void nonDragonDoesNotConsumeReduction() {
        harness.setHand(player1, List.of(new DraconicDebut(), new GrizzlyBears(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Killing the target still creates the Dragon reduction")
    void lethalDamageStillCreatesReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An illegal target prevents the Dragon reduction")
    void illegalTargetPreventsReduction() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, 2, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Draconic Debut");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero deals no damage and grants no discount")
    void zeroXGrantsNoDiscount() {
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Debuts combine on the same next Dragon")
    void multipleReductionsCombineAndAreConsumedTogether() {
        harness.setHand(player1, List.of(new DraconicDebut(), new DraconicDebut(),
                new ChardalynDragon(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reduction greater than the Dragon cost makes it free")
    void reductionCannotMakeCostNegative() {
        harness.setHand(player1, List.of(new DraconicDebut(), new ChardalynDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player1, 0, 8, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chardalyn Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
