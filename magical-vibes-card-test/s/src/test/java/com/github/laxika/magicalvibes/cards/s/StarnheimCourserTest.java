package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.r.RuneOfSustenance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarnheimCourser.class, SylvokLifestaff.class, GhostlyPrison.class,
        GrizzlyBears.class, GoldveinPick.class, RuneOfSustenance.class})
class StarnheimCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells you cast cost {1} less")
    void artifactSpellsAreReduced() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Enchantment spells you cast cost {1} less")
    void enchantmentSpellsAreReduced() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghostly Prison");
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's artifact spells are not reduced")
    void opponentArtifactSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player2, List.of(new SylvokLifestaff()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Coursers stack their reductions")
    void multipleCoursersStack() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new GoldveinPick()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldvein Pick");
    }

    @Test
    @DisplayName("One Courser reduces a two-mana artifact to one mana")
    void artifactStillRequiresRemainingGenericMana() {
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new GoldveinPick()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldvein Pick");
    }

    @Test
    @DisplayName("Excess generic reductions cannot pay an enchantment's colored cost")
    void coloredManaCannotBeReduced() {
        var courser = harness.addToBattlefieldAndReturn(player1, new StarnheimCourser());
        harness.addToBattlefield(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new RuneOfSustenance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, courser.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, courser.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Rune of Sustenance");
    }

    @Test
    @DisplayName("Cost reduction ends when Courser leaves the battlefield")
    void reductionEndsWhenCourserDies() {
        var courser = harness.addToBattlefieldAndReturn(player1, new StarnheimCourser());
        harness.setHand(player1, List.of(new GoldveinPick()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        courser.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Starnheim Courser");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldvein Pick");
    }
}
