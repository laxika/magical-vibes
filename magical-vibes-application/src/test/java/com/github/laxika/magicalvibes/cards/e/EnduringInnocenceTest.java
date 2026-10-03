package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InquisitiveGlimmer;
import com.github.laxika.magicalvibes.cards.l.LeylineOfHope;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringInnocence.class, GrizzlyBears.class, AirElemental.class,
        DoomBlade.class, Disenchant.class, OneWithTheStars.class, Forest.class,
        InquisitiveGlimmer.class, LeylineOfHope.class})
class EnduringInnocenceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life from combat damage while it is a creature")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EnduringInnocence());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Draws once each turn for another creature you control with power 2 or less")
    void drawsOncePerTurnForSmallAlly() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(2);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 2 + 1);
    }

    @Test
    @DisplayName("Does not draw for an opponent's creature or a creature with power greater than 2")
    void ignoresOpponentsAndLargeCreatures() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(2);

        harness.setHand(player1, List.of(new AirElemental()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment and not a creature")
    void returnsAsEnchantmentOnly() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        Permanent enduring = findPermanent(player1, "Enduring Innocence");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Innocence");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItWasNotACreature() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        Permanent enduring = findPermanent(player1, "Enduring Innocence");

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, enduring.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, enduring)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enduring Innocence");
        harness.assertNotOnBattlefield(player1, "Enduring Innocence");
    }

    @Test
    @DisplayName("Does not draw for its own entry")
    void doesNotDrawForItself() {
        seedLibrary(2);
        harness.setHand(player1, List.of(new EnduringInnocence()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Enduring Innocence");
    }

    @Test
    @DisplayName("Uses power including continuous bonuses at the moment of entry")
    void doesNotDrawForCreatureBoostedAboveTwoOnEntry() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.setLife(player1, 27);
        seedLibrary(2);
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Inquisitive Glimmer"))).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still draws when the entering creature leaves before the trigger resolves")
    void drawsAfterEnteringCreatureDies() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(2);
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Inquisitive Glimmer").getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inquisitive Glimmer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each copy has its own once-per-turn draw trigger")
    void copiesTriggerIndependently() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(3);
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw allowance resets on the next turn")
    void canDrawAgainNextTurn() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(4);
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN, player2);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN, player1);
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A returned enchantment can draw again that turn as a new permanent")
    void returnedEnchantmentHasFreshTriggerAllowance() {
        harness.addToBattlefield(player1, new EnduringInnocence());
        seedLibrary(3);
        harness.setHand(player1, List.of(new InquisitiveGlimmer(), new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new DoomBlade(), new Disenchant()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Enduring Innocence").getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Enduring Innocence"))).isFalse();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Enduring Innocence").getId());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Enduring Innocence");
        harness.assertNotOnBattlefield(player1, "Enduring Innocence");
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Forest()).toList());
    }
}
