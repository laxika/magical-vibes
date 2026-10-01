package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheStars.class, DryadSophisticate.class, DouseInGloom.class})
class OrderOfTheStarsTest extends BaseCardTest {

    @Test
    void choosesAColorAsItEnters() {
        harness.setHand(player1, List.of(new OrderOfTheStars()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Order of the Stars").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    void preventsCombatDamageFromTheChosenColor() {
        Permanent order = addCreatureReady(player2, new OrderOfTheStars());
        order.setChosenColor(CardColor.GREEN);
        order.setBlocking(true);
        order.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Order of the Stars");
    }

    @Test
    void takesCombatDamageFromOtherColors() {
        Permanent order = addCreatureReady(player2, new OrderOfTheStars());
        order.setChosenColor(CardColor.RED);
        order.setBlocking(true);
        order.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Order of the Stars");
        harness.assertInGraveyard(player2, "Order of the Stars");
    }

    @Test
    void protectionFromChosenColorPreventsMatchingSpellTargeting() {
        harness.setHand(player1, List.of(new OrderOfTheStars()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        Permanent order = findPermanent(player1, "Order of the Stars");
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
