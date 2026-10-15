package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PheresBandBrawler.class, GrizzlyBears.class, SternDismissal.class, PolukranosUnchained.class})
class PheresBandBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB fights up to one target creature an opponent controls")
    void entersAndFightsTargetCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent brawler = castBrawler();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(brawler.getId()));
    }

    @Test
    @DisplayName("ETB resolves without a target when an opponent controls no creatures")
    void entersWithoutTarget() {
        Permanent brawler = castBrawler();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(brawler.getId()));
    }

    @Test
    void canChooseNoTargetEvenWhenAnOpponentControlsACreature() {
        Permanent opponentCreature = addCreatureReady(player2, new PheresBandBrawler());
        Permanent brawler = castBrawler();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pheres-Band Brawler");
        harness.assertOnBattlefield(player2, "Pheres-Band Brawler");
        assertThat(brawler.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    void cannotChooseACreatureYouControl() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new PheresBandBrawler());
        castBrawler();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Pheres-Band Brawler");
        harness.assertInGraveyard(player2, "Pheres-Band Brawler");
    }

    @Test
    void bothCreaturesDealLethalDamageWhenTheyFight() {
        Permanent opponentCreature = addCreatureReady(player2, new PheresBandBrawler());
        castBrawler();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pheres-Band Brawler");
        harness.assertNotOnBattlefield(player2, "Pheres-Band Brawler");
        harness.assertInGraveyard(player1, "Pheres-Band Brawler");
        harness.assertInGraveyard(player2, "Pheres-Band Brawler");
    }

    @Test
    void neitherCreatureDealsDamageIfBrawlerLeavesBeforeResolution() {
        Permanent opponentCreature = addCreatureReady(player2, new PheresBandBrawler());
        Permanent brawler = castBrawler();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, brawler.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pheres-Band Brawler");
        harness.assertOnBattlefield(player2, "Pheres-Band Brawler");
        assertThat(opponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    void fightUsesTargetsPowerBeforeDamageRemovesItsCounters() {
        Permanent polukranos = harness.enterBattlefieldAndReturn(player2, new PolukranosUnchained());
        resolveAllTriggers();
        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        castBrawler();

        harness.handlePermanentChosen(player1, polukranos.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pheres-Band Brawler");
        harness.assertInGraveyard(player1, "Pheres-Band Brawler");
        harness.assertOnBattlefield(player2, "Polukranos, Unchained");
        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(polukranos.getMarkedDamage()).isZero();
    }

    @Test
    void neitherCreatureDealsDamageIfTargetLeavesBeforeResolution() {
        Permanent opponentCreature = addCreatureReady(player2, new PheresBandBrawler());
        Permanent brawler = castBrawler();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        harness.setHand(player1, List.of(new SternDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Pheres-Band Brawler");
        harness.assertOnBattlefield(player1, "Pheres-Band Brawler");
        assertThat(brawler.getMarkedDamage()).isZero();
    }

    private Permanent castBrawler() {
        harness.castFromHand(player1, new PheresBandBrawler(), "{4}{G}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Pheres-Band Brawler");
    }
}
