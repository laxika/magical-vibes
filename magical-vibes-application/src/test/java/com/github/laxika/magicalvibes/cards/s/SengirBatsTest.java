package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SengirBats.class, FugitiveWizard.class, GrizzlyBears.class, CruelEdict.class,
        SeaSprite.class, SengirAutocrat.class})
class SengirBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        Permanent bats = addCreatureReady(player1, new SengirBats());
        bats.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        assertThat(bats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bats)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers when a creature damaged by Sengir Bats dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent bats = addCreatureReady(player1, new SengirBats());
        bats.setAttacking(true);

        GrizzlyBears toughBlockerCard = new GrizzlyBears();
        toughBlockerCard.setPower(1);
        toughBlockerCard.setToughness(5);
        Permanent blocker = addCreatureReady(player2, toughBlockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(bats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(bats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only for a creature damaged by that Sengir Bats")
    void triggersOnlyForCreatureDamagedByThatSengirBats() {
        Permanent watchingBats = addCreatureReady(player1, new SengirBats());
        Permanent damagingBats = addCreatureReady(player1, new SengirBats());
        damagingBats.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SeaSprite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(watchingBats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(damagingBats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when a creature token it damaged dies")
    void triggersWhenDamagedCreatureTokenDies() {
        Permanent bats = addCreatureReady(player1, new SengirBats());
        harness.enterBattlefieldAndReturn(player2, new SengirAutocrat());
        resolveAllTriggers();

        Permanent serf = findPermanents(player2, "Serf").getFirst();
        serf.setAttacking(true);
        bats.setBlocking(true);
        bats.addBlockingTarget(gd.playerBattlefields.get(player2.getId()).indexOf(serf));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Serf")).doesNotContain(serf);
        assertThat(bats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
