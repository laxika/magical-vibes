package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DogmeatEverLoyal;
import com.github.laxika.magicalvibes.cards.g.GlowingOne;
import com.github.laxika.magicalvibes.cards.h.HancockGhoulishMayor;
import com.github.laxika.magicalvibes.cards.s.StrongTheBrutishThespian;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasonBrightGlowingProphet.class, GlowingOne.class, DogmeatEverLoyal.class,
        HancockGhoulishMayor.class, StrongTheBrutishThespian.class})
class JasonBrightGlowingProphetTest extends BaseCardTest {

    @Test
    @DisplayName("A modified Zombie dying draws a card, and the ability counters and grants flying")
    void modifiedZombieDrawsAndAbilityResolves() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new GlowingOne());
        zombie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(jason.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(jason.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Glowing One");
    }

    @Test
    @DisplayName("A modified Zombie with lower power also satisfies the death condition")
    void lowerModifiedZombieDraws() {
        addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new GlowingOne());
        zombie.setPowerModifier(-1);
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("An unmodified Zombie dying does not draw a card")
    void unmodifiedZombieDoesNotDraw() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new GlowingOne());
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
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
        Permanent bears = addCreatureReady(player1, new DogmeatEverLoyal());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
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
        Permanent fodder = addCreatureReady(player1, new DogmeatEverLoyal());
        Permanent opponentCreature = addCreatureReady(player2, new DogmeatEverLoyal());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jason, fodder);
    }

    @Test
    @DisplayName("Jason's own modified death draws exactly one card")
    void modifiedJasonDrawsOnOwnDeath() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent target = addCreatureReady(player1, new DogmeatEverLoyal());
        jason.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
        DogmeatEverLoyal remaining = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(drawn, remaining));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, jason.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Jason Bright, Glowing Prophet");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A modified Mutant without the Zombie subtype draws a card")
    void modifiedMutantDraws() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent mutant = addCreatureReady(player1, new StrongTheBrutishThespian());
        mutant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, mutant.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Power modified only by Hancock's static bonus still draws on death")
    void staticPowerBonusAtDeathDraws() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent hancock = addCreatureReady(player1, new HancockGhoulishMayor());
        hancock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent zombie = addCreatureReady(player1, new GlowingOne());
        DogmeatEverLoyal drawn = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Flying expires at cleanup while the counter remains")
    void flyingExpiresButCounterRemains() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent fodder = addCreatureReady(player1, new DogmeatEverLoyal());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();
        assertThat(jason.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(jason.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(jason.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifiers that cancel out do not satisfy the power condition")
    void netUnchangedPowerDoesNotDraw() {
        Permanent jason = addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player1, new GlowingOne());
        zombie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        zombie.setPowerModifier(-1);
        DogmeatEverLoyal libraryCard = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(libraryCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, jason.getId());
        harness.handlePermanentChosen(player1, zombie.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("An opponent's modified Zombie dying does not draw a card")
    void opponentZombieDoesNotDraw() {
        addCreatureReady(player1, new JasonBrightGlowingProphet());
        Permanent zombie = addCreatureReady(player2, new GlowingOne());
        zombie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DogmeatEverLoyal libraryCard = new DogmeatEverLoyal();
        harness.setLibrary(player1, java.util.List.of(libraryCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zombie);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player2, "Glowing One");
    }
}
