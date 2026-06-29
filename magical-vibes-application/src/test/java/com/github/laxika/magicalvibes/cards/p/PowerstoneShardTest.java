package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PowerstoneShardTest extends BaseCardTest {

    @Test
    @DisplayName("Powerstone Shard has one tap-for-mana activated ability")
    void hasCorrectAbility() {
        PowerstoneShard card = new PowerstoneShard();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AddManaPerControlledPermanentEffect.class);
    }

    @Test
    @DisplayName("With one Powerstone Shard, tapping adds 1 colorless mana")
    void oneShard() {
        harness.addToBattlefield(player1, new PowerstoneShard());

        Permanent shard = gd.playerBattlefields.get(player1.getId()).getFirst();
        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("With three Powerstone Shards, tapping one adds 3 colorless mana")
    void threeShards() {
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());

        Permanent shard = gd.playerBattlefields.get(player1.getId()).getFirst();
        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's Powerstone Shards")
    void doesNotCountOpponentShards() {
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player2, new PowerstoneShard());
        harness.addToBattlefield(player2, new PowerstoneShard());

        Permanent shard = gd.playerBattlefields.get(player1.getId()).getFirst();
        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        // Only player1's 1 shard, not opponent's 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count non-artifact permanents with the same name")
    void doesNotCountNonArtifacts() {
        harness.addToBattlefield(player1, new PowerstoneShard());

        // Add a second shard but change its type to non-artifact to simulate a type-changing effect
        PowerstoneShard nonArtifact = new PowerstoneShard();
        nonArtifact.setType(com.github.laxika.magicalvibes.model.CardType.ENCHANTMENT);
        harness.addToBattlefield(player1, nonArtifact);

        Permanent shard = gd.playerBattlefields.get(player1.getId()).getFirst();
        shard.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        // Only the real artifact counts
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
