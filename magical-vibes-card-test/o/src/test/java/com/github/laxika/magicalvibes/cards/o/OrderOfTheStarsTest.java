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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheStars.class, DryadSophisticate.class, DouseInGloom.class})
class OrderOfTheStarsTest extends BaseCardTest {

    @Test
    void choosesAColorAsItEnters() {
        harness.castFromHand(player1, new OrderOfTheStars(), "{W}");
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
        harness.castFromHand(player1, new OrderOfTheStars(), "{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        Permanent order = findPermanent(player1, "Order of the Stars");
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    void canChooseEachColorWithoutCreatingATrigger(CardColor color) {
        harness.castFromHand(player1, new OrderOfTheStars(), "{W}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, color.name());

        assertThat(findPermanent(player1, "Order of the Stars").getChosenColor()).isEqualTo(color);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new OrderOfTheStars()).setChosenColor(CardColor.GREEN);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Order of the Stars").isAttacking()).isFalse();
    }

    @Test
    void protectionAlsoPreventsItsControllersMatchingSpellTargeting() {
        harness.castFromHand(player1, new OrderOfTheStars(), "{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        Permanent order = findPermanent(player1, "Order of the Stars");
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    void otherColoredSpellCanTargetAndDealLethalDamage() {
        harness.castFromHand(player1, new OrderOfTheStars(), "{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        Permanent order = findPermanent(player1, "Order of the Stars");
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, order.getId());

        harness.assertNotOnBattlefield(player1, "Order of the Stars");
        harness.assertInGraveyard(player1, "Order of the Stars");
        harness.assertLife(player2, 22);
    }

    @Test
    void separatePermanentsChooseIndependentlyWhenEnteringWithoutBeingCast() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new OrderOfTheStars());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLACK");

        Permanent second = harness.enterBattlefieldAndReturn(player1, new OrderOfTheStars());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(first.getChosenColor()).isEqualTo(CardColor.BLACK);
        assertThat(second.getChosenColor()).isEqualTo(CardColor.GREEN);

        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, first.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");

        harness.castAndResolveInstant(player2, 0, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
    }
}
