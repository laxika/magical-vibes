package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfLightning.class, ChandraNalaar.class, GrizzlyBears.class, Shock.class})
class LeylineOfLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfLightning()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Lightning"));
    }

    @Test
    @DisplayName("Paying {1} after casting a spell deals 1 damage to a player")
    void payingManaDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Leyline damage can target a planeswalker")
    void payingManaDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the payment deals no damage")
    void decliningPaymentDealsNoDamage() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
