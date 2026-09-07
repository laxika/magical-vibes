package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientAdamantoise.class, GrizzlyBears.class, Shock.class})
class AncientAdamantoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to its controller is dealt to Ancient Adamantoise instead")
    void damageToControllerRedirectedToAncientAdamantoise() {
        Permanent ancient = addCreatureReady(player2, new AncientAdamantoise());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(ancient.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to another permanent it controls is dealt to Ancient Adamantoise instead")
    void damageToControlledPermanentRedirectedToAncientAdamantoise() {
        Permanent ancient = addCreatureReady(player2, new AncientAdamantoise());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(ancient.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage marked on Ancient Adamantoise remains through cleanup")
    void damageRemainsMarkedThroughCleanup() {
        Permanent ancient = addCreatureReady(player1, new AncientAdamantoise());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        ancient.setMarkedDamage(3);
        ancient.setDamagedByDeathtouch(true);
        bears.setMarkedDamage(1);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(ancient.getMarkedDamage()).isEqualTo(3);
        assertThat(ancient.isDamagedByDeathtouch()).isTrue();
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("When Ancient Adamantoise dies, it is exiled and creates ten tapped Treasures")
    void deathExilesItAndCreatesTappedTreasures() {
        Permanent ancient = addCreatureReady(player1, new AncientAdamantoise());
        ancient.setMarkedDamage(20);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ancient.getCard().getId()));
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(10);
        assertThat(treasures).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }
}
