package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrderOfTheSacredTorch.class, DarkBanishing.class, KjeldoranWarrior.class, Incinerate.class})
class OrderOfTheSacredTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target black spell and pays 1 life")
    void countersBlackSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        Permanent victim = harness.addToBattlefieldAndReturn(player1, new KjeldoranWarrior());

        DarkBanishing banishing = new DarkBanishing();
        harness.setHand(player2, List.of(banishing));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, victim.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, banishing.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player2, "Dark Banishing");
        harness.assertOnBattlefield(player1, "Kjeldoran Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a red spell")
    void cannotTargetRedSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        var orderPermanent = addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, incinerate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(orderPermanent.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
