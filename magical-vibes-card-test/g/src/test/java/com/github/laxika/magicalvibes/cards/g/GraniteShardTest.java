package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraniteShard.class, GoldMyr.class, Shatter.class})
class GraniteShardTest extends BaseCardTest {

    @Test
    @DisplayName("The generic activation deals 1 damage to a target player")
    void genericActivationDealsDamageToTargetPlayer() {
        Permanent shard = addReadyShard();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shard.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The red activation deals 1 damage to a target creature")
    void redActivationDealsDamageToTargetCreature() {
        Permanent shard = addReadyShard();
        Permanent target = addCreatureReady(player2, new GoldMyr());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(shard.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Gold Myr");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either payment can activate a newly entered artifact and pays costs before damage resolves")
    void newlyEnteredShardPaysCostsBeforeResolution(int abilityIndex) {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setLife(player1, 20);
        addActivationMana(abilityIndex);

        harness.activateAbility(player1, 0, abilityIndex, null, player1.getId());

        assertThat(shard.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @ParameterizedTest
    @CsvSource({"0, COLORLESS, 2", "1, COLORLESS, 3"})
    @DisplayName("Activation requires the full chosen mana cost without consuming costs on failure")
    void insufficientManaDoesNotTapShard(int abilityIndex, ManaColor color, int amount) {
        Permanent shard = addReadyShard();
        harness.addMana(player1, color, amount);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shard.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(amount);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A tapped shard cannot use the other payment option for a second activation")
    void otherPaymentCannotBypassTapCost(int abilityIndex) {
        Permanent shard = addReadyShard();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, abilityIndex, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shard.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither payment option can target a noncreature artifact")
    void cannotTargetNoncreatureArtifact(int abilityIndex) {
        Permanent shard = addReadyShard();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GraniteShard());
        addActivationMana(abilityIndex);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shard.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Damage still resolves after the shard is destroyed in response")
    void damageResolvesAfterSourceIsDestroyed(int abilityIndex) {
        Permanent shard = addReadyShard();
        harness.setLife(player2, 20);
        addActivationMana(abilityIndex);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, player2.getId());
        harness.castInstant(player2, 0, shard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Granite Shard");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An ability whose creature target leaves does not damage another creature")
    void removedTargetDoesNotRedirectDamage(int abilityIndex) {
        Permanent shard = addReadyShard();
        Permanent target = addCreatureReady(player2, new GoldMyr());
        addCreatureReady(player2, new GoldMyr());
        addActivationMana(abilityIndex);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Gold Myr");

        harness.passBothPriorities();

        assertThat(shard.isTapped()).isTrue();
        assertThat(countPermanents(player2, "Gold Myr")).isEqualTo(1);
        assertThat(findPermanent(player2, "Gold Myr").getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana(int abilityIndex) {
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.COLORLESS : ManaColor.RED,
                abilityIndex == 0 ? 3 : 1);
    }

    private Permanent addReadyShard() {
        return addCreatureReady(player1, new GraniteShard());
    }
}
