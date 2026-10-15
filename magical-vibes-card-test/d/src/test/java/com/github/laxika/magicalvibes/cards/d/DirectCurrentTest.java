package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RalIzzetViceroy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirectCurrent.class, GrizzlyBears.class, Plains.class, RalIzzetViceroy.class})
class DirectCurrentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsTwoDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new DirectCurrent()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Jump-start discards a card, deals 2 damage, and exiles the spell")
    void jumpStartDiscardsDealsDamageAndExiles() {
        DirectCurrent spell = new DirectCurrent();
        Plains discarded = new Plains();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Casting from hand can damage its controller and puts the spell in the graveyard")
    void normalCastCanTargetController() {
        harness.setHand(player1, List.of(new DirectCurrent()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Direct Current");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker")
    void damagesPlaneswalker() {
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new DirectCurrent()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ral.getId());

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new DirectCurrent()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Direct Current");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Jump-start still requires the normal mana cost")
    void jumpStartRequiresMana() {
        harness.setGraveyard(player1, List.of(new DirectCurrent()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Direct Current");
        harness.assertInHand(player1, "Plains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jump-start exiles the spell even when its target becomes illegal")
    void jumpStartExilesWhenTargetDisappears() {
        DirectCurrent spell = new DirectCurrent();
        Plains discarded = new Plains();
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, ral.getId());
        harness.assertNotInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Plains");
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Direct Current");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
