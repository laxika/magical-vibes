package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurtleLair.class, GrizzlyBears.class, HornedTurtle.class})
class TurtleLairTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent lair = addReadyLair();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds one mana of a chosen color for Ninja or Turtle spells")
    void secondAbilityAddsNinjaOrTurtleSpellOnlyMana() {
        Permanent lair = addReadyLair();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.BLUE)).isZero();
        assertThat(pool().getSubtypeSpellOnlyManaForColor(
                Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ninja or Turtle spell-only mana can cast a matching spell")
    void restrictedManaCanCastMatchingSpell() {
        pool().addSubtypeSpellOnlyMana(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(createCreature("Test Turtle", "{U}", CardSubtype.TURTLE)));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isZero();
    }

    @Test
    @DisplayName("Ninja or Turtle spell-only mana cannot cast another spell")
    void restrictedManaCannotCastOtherSpell() {
        pool().addSubtypeSpellOnlyMana(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(createCreature("Test Bear", "{U}", CardSubtype.BEAR)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isEqualTo(1);
    }

    @Test
    @DisplayName("The third ability makes a target Turtle unable to be blocked this turn")
    void thirdAbilityMakesTargetUnblockable() {
        addReadyLair();
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, turtle.getId());
        harness.passBothPriorities();

        assertThat(turtle.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addReadyLair();
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, turtle.getId());
        harness.passBothPriorities();
        assertThat(turtle.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(turtle.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The third ability cannot target a creature without the Ninja or Turtle subtype")
    void thirdAbilityRejectsOtherCreature() {
        addReadyLair();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ninja or Turtle");
    }

    private Permanent addReadyLair() {
        Permanent lair = harness.addToBattlefieldAndReturn(player1, new TurtleLair());
        lair.setSummoningSick(false);
        return lair;
    }

    private ManaPool pool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private static Card createCreature(String name, String manaCost, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
