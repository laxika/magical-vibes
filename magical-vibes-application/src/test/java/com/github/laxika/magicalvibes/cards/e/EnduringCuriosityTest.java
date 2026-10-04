package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringCuriosity.class, GrizzlyBears.class, SerraAngel.class,
        DoomBlade.class, Disenchant.class, OneWithTheStars.class, Forest.class})
class EnduringCuriosityTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature you control deals combat damage to a player")
    void drawsForAllyCombatDamage() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        addReadyAttacker(player1, new GrizzlyBears());
        seedLibrary(1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when the creature is blocked")
    void doesNotDrawWhenBlocked() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        addReadyAttacker(player1, new GrizzlyBears());
        addReadyBlocker(player2, new SerraAngel(), 1);
        seedLibrary(1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment and not a creature")
    void returnsAsEnchantmentOnly() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        Permanent curiosity = findPermanent(player1, "Enduring Curiosity");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, curiosity.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Curiosity");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItWasNotACreature() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        Permanent curiosity = findPermanent(player1, "Enduring Curiosity");

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, curiosity.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, curiosity)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, curiosity.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enduring Curiosity");
        harness.assertNotOnBattlefield(player1, "Enduring Curiosity");
    }

    @Test
    @DisplayName("Can be cast during the opponent's upkeep using flash")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castFromHand(player1, new EnduringCuriosity(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Enduring Curiosity");
    }

    @Test
    @DisplayName("Returns under its owner's control after dying under another player's control")
    void returnsToOwnerRatherThanLastController() {
        EnduringCuriosity card = new EnduringCuriosity();
        card.setOwnerId(player1.getId());
        Permanent curiosity = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, curiosity.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Enduring Curiosity");
        Permanent returned = findPermanent(player1, "Enduring Curiosity");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
    }

    @Test
    @DisplayName("Draws for its own combat damage")
    void drawsForItsOwnCombatDamage() {
        addReadyAttacker(player1, new EnduringCuriosity());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Draws once for each creature dealing combat damage, not once per damage step")
    void drawsForEachDamagingCreature() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        addReadyAttacker(player1, new GrizzlyBears());
        addReadyAttacker(player1, new GrizzlyBears());
        seedLibrary(2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Does not draw for an opponent's creature dealing combat damage")
    void doesNotDrawForOpposingCreature() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        addReadyAttacker(player2, new SerraAngel());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Retains the draw ability after returning as an enchantment")
    void returnedEnchantmentStillDraws() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        Permanent curiosity = findPermanent(player1, "Enduring Curiosity");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, curiosity.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Enduring Curiosity"))).isFalse();

        addReadyAttacker(player1, new GrizzlyBears());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not return a second time when destroyed as its returned enchantment")
    void returnedEnchantmentStaysInGraveyard() {
        harness.addToBattlefield(player1, new EnduringCuriosity());
        Permanent curiosity = findPermanent(player1, "Enduring Curiosity");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, curiosity.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Enduring Curiosity");
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Enduring Curiosity");
        harness.assertNotOnBattlefield(player1, "Enduring Curiosity");
    }

    private void addReadyAttacker(Player player, Card card) {
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
    }

    private void addReadyBlocker(Player player, Card card, int attackerIndex) {
        Permanent blocker = addCreatureReady(player, card);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(attackerIndex);
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Forest()).toList());
    }
}
