package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheSacredTorch.class, GrizzlyBears.class, Terror.class, Incinerate.class, DarkRitual.class})
class OrderOfTheSacredTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target black spell and pays 1 life")
    void countersBlackSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        Permanent orderPermanent = addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        GrizzlyBears victim = new GrizzlyBears();
        harness.addToBattlefield(player1, victim);

        Terror terror = new Terror();
        harness.setHand(player2, List.of(terror));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, terror.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Terror");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(orderPermanent.isTapped()).isTrue();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a red spell")
    void cannotTargetRedSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        Permanent orderPermanent = addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, incinerate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(orderPermanent.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can counter its controller's own black spell")
    void countersItsControllersOwnBlackSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        Permanent orderPermanent = addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, null, ritual.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
        assertThat(orderPermanent.isTapped()).isTrue();
        harness.assertLife(player1, 19);
    }
}
