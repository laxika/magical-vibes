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

    private void addReadyAttacker(Player player, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }

    private void addReadyBlocker(Player player, Card card, int attackerIndex) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(player.getId()).add(blocker);
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }
}
