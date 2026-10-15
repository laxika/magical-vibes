package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({FlayerOfTheHatebound.class, LightningBolt.class, ReassemblingSkeleton.class, WhiteKnight.class})
class FlayerOfTheHateboundTest extends BaseCardTest {

    private Permanent flayerOnBattlefield(GameData gd) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Flayer of the Hatebound"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Undying returns Flayer with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flayer of the Hatebound"));
        resolveAllTriggers();

        // Bolt killed Flayer; undying returned it with a +1/+1 counter and its
        // enters-from-graveyard ability is now asking for a target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent flayer = flayerOnBattlefield(gd);
        assertThat(flayer).isNotNull();
        assertThat(flayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flayer.getEffectivePower()).isEqualTo(5);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Undying does not return Flayer when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent flayer = harness.addToBattlefieldAndReturn(player1, new FlayerOfTheHatebound());
        flayer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // now 5/3
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, flayer.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flayer of the Hatebound");
        harness.assertNotOnBattlefield(player1, "Flayer of the Hatebound");
    }

    @Test
    @DisplayName("Flayer's undying return deals damage equal to its power (5) to a chosen player")
    void selfReturnDealsPowerDamageToPlayer() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flayer of the Hatebound"));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // Returned Flayer is a 5/3; the trigger deals 5 to player2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Flayer's undying return can deal its power as damage to a creature")
    void selfReturnDealsPowerDamageToCreature() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // A creature for player2 that the 5-power trigger will kill.
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ReassemblingSkeleton());

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flayer of the Hatebound"));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Another creature entering from your graveyard triggers Flayer")
    void anotherCreatureFromGraveyardTriggers() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        // Skeleton returned from player1's graveyard → Flayer triggers, awaiting a target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // Reassembling Skeleton is a 1/1, so it deals 1 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("The returning creature is the damage source for protection")
    void returningBlackCreatureCannotDamageWhiteKnight() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        // The red Flayer's ability can target White Knight, but the black Skeleton's damage is prevented.
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage uses the returning creature's power at resolution")
    void returningCreaturePowerIsReadAtResolution() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent skeleton = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Reassembling Skeleton"))
                .findFirst().orElseThrow();
        skeleton.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The damage trigger survives the returning creature dying and uses last-known power")
    void removedReturningCreatureStillDealsDamage() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent skeleton = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Reassembling Skeleton"))
                .findFirst().orElseThrow();
        skeleton.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castInstant(player1, 0, skeleton.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature returning from an opponent's graveyard does not trigger Flayer")
    void opponentGraveyardDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setGraveyard(player2, List.of(new ReassemblingSkeleton()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Reassembling Skeleton");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
