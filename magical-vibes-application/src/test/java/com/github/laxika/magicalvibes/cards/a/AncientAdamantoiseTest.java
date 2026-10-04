package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tonberry;
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

@CardUsed({AncientAdamantoise.class, GrizzlyBears.class, Shock.class, Tonberry.class})
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
        bears.setMarkedDamage(1);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(ancient.getMarkedDamage()).isEqualTo(3);
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
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(10);
        assertThat(treasures).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Treasures are created even if Ancient Adamantoise leaves the graveyard before resolution")
    void createsTreasuresWhenSourceHasLeftGraveyard() {
        Permanent ancient = addCreatureReady(player1, new AncientAdamantoise());
        ancient.setMarkedDamage(20);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ancient.getCard());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(ancient.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ancient.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(10)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Losing its abilities stops redirecting damage from other permanents")
    void losingAbilitiesStopsPermanentDamageRedirection() {
        Permanent ancient = addCreatureReady(player2, new AncientAdamantoise());
        ancient.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(ancient.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Damage to the opponent is not redirected")
    void opponentDamageIsNotRedirected() {
        Permanent ancient = addCreatureReady(player1, new AncientAdamantoise());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(ancient.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Redirected combat damage retains deathtouch")
    void redirectedCombatDeathtouchKillsAncientAdamantoise() {
        addCreatureReady(player1, new Tonberry());
        Permanent ancient = addCreatureReady(player2, new AncientAdamantoise());

        declareAttackers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ancient);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(ancient.getCard());
        assertThat(findPermanents(player2, "Treasure")).hasSize(10)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }
}
