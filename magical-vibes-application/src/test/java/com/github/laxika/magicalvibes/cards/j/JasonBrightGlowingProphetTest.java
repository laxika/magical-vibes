package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieBrute;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasonBrightGlowingProphet.class, ZombieBrute.class, GrizzlyBears.class})
class JasonBrightGlowingProphetTest extends BaseCardTest {

    @Test
    @DisplayName("A modified Zombie dying draws a card, and the ability counters and grants flying")
    void modifiedZombieDrawsAndAbilityResolves() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        zombie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(jason.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(jason.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Zombie Brute");
    }

    @Test
    @DisplayName("A modified Zombie with lower power also satisfies the death condition")
    void lowerModifiedZombieDraws() {
        addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        zombie.setPowerModifier(-1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("An unmodified Zombie dying does not draw a card")
    void unmodifiedZombieDoesNotDraw() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new ZombieBrute());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, java.util.List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A modified non-Zombie and non-Mutant dying does not draw a card")
    void modifiedNonZombieDoesNotDraw() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, java.util.List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void targetMustBeControlledCreature() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jason, fodder);
    }
}
