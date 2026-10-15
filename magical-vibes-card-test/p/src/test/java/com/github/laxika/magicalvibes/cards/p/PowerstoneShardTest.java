package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MairsilThePretender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerstoneShard.class, MairsilThePretender.class})
class PowerstoneShardTest extends BaseCardTest {

    @Test
    @DisplayName("With one Powerstone Shard, tapping adds 1 colorless mana")
    void oneShard() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());

        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("With three Powerstone Shards, tapping one adds 3 colorless mana")
    void threeShards() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());

        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's Powerstone Shards")
    void doesNotCountOpponentShards() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());
        harness.addToBattlefield(player2, new PowerstoneShard());
        harness.addToBattlefield(player2, new PowerstoneShard());

        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        // Only player1's 1 shard, not opponent's 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count non-artifact permanents with the same name")
    void doesNotCountNonArtifacts() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());

        // Add a second shard but change its type to non-artifact to simulate a type-changing effect
        PowerstoneShard nonArtifact = new PowerstoneShard();
        nonArtifact.setType(com.github.laxika.magicalvibes.model.CardType.ENCHANTMENT);
        harness.addToBattlefield(player1, nonArtifact);

        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        // Only the real artifact counts
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly entered noncreature Shard produces mana immediately without using the stack")
    void newlyEnteredShardProducesManaImmediately() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shard.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped Shards still count and each Shard can produce mana")
    void tappedShardsStillCount() {
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shards outside the battlefield do not count")
    void shardsOutsideBattlefieldDoNotCount() {
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.setHand(player1, List.of(new PowerstoneShard()));
        harness.setGraveyard(player1, List.of(new PowerstoneShard()));
        harness.setExile(player1, List.of(new PowerstoneShard()));
        harness.setLibrary(player1, List.of(new PowerstoneShard()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mairsil's gained ability counts artifacts named Powerstone Shard")
    void gainedAbilityStillCountsPowerstoneShards() {
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());
        PowerstoneShard cagedShard = new PowerstoneShard();
        harness.setHand(player1, List.of(cagedShard));
        Permanent mairsil = harness.enterBattlefieldAndReturn(player1, new MairsilThePretender());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(cagedShard.getId()));
        mairsil.setSummoningSick(false);

        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(mairsil.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
