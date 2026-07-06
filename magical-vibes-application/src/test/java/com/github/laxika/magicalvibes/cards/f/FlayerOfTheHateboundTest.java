package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class FlayerOfTheHateboundTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gd = harness.getGameData();
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent flayerOnBattlefield(GameData gd) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Flayer of the Hatebound"))
                .findFirst().orElse(null);
    }

    // ===== Undying =====

    @Test
    @DisplayName("Undying returns Flayer with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flayer of the Hatebound"));
        resolveUntilInputOrEmpty();

        // Bolt killed Flayer; undying returned it with a +1/+1 counter and its
        // enters-from-graveyard ability is now asking for a target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent flayer = flayerOnBattlefield(gd);
        assertThat(flayer).isNotNull();
        assertThat(flayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flayer.getEffectivePower()).isEqualTo(5);
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
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flayer of the Hatebound"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Flayer of the Hatebound"));
    }

    // ===== Enters-from-graveyard trigger (self via undying) =====

    @Test
    @DisplayName("Flayer's undying return deals damage equal to its power (5) to a chosen player")
    void selfReturnDealsPowerDamageToPlayer() {
        harness.addToBattlefield(player1, new FlayerOfTheHatebound());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Flayer of the Hatebound"));
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

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
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    // ===== Enters-from-graveyard trigger (another creature) =====

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
        resolveUntilInputOrEmpty();

        // Skeleton returned from player1's graveyard → Flayer triggers, awaiting a target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        // Reassembling Skeleton is a 1/1, so it deals 1 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reassembling Skeleton"));
    }
}
