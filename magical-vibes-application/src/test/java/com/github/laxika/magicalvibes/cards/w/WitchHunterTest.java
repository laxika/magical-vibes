package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchHunter.class, Squire.class, DovinGrandArbiter.class})
class WitchHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent hunter = addCreatureReady(player1, new WitchHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        Permanent hunter = addCreatureReady(player1, new WitchHunter());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns target creature an opponent controls to its owner's hand")
    void returnsOpponentsCreatureToHand() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());
        addBounceMana(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertInHand(player2, "Squire");
    }

    @Test
    @DisplayName("Returns an opponent-controlled creature to its owner's hand")
    void returnsTargetToItsOwnersHand() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Squire targetCard = new Squire();
        targetCard.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player2, targetCard);
        addBounceMana(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertInHand(player1, "Squire");
        harness.assertNotInHand(player2, "Squire");
    }

    @Test
    @DisplayName("Cannot activate the bounce ability without enough mana")
    void cannotActivateBounceAbilityWithoutEnoughMana() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player1, new Squire());
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a creature with the damage ability")
    void damageAbilityCannotTargetCreature() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a planeswalker with the bounce ability")
    void bounceAbilityCannotTargetPlaneswalker() {
        addCreatureReady(player1, new WitchHunter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        target.setCounterCount(CounterType.LOYALTY, 3);
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void cannotActivateEitherAbilityWhileSummoningSick() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        hunter.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new Squire());
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hunter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while tapped")
    void cannotActivateEitherAbilityWhileTapped() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        hunter.tap();
        Permanent target = addCreatureReady(player2, new Squire());
        addBounceMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bounce requires two white mana even with enough total mana")
    void cannotPayBounceCostWithOnlyOneWhiteMana() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hunter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Squire");
    }

    @Test
    @DisplayName("Damage resolves after Witch Hunter leaves the battlefield")
    void damageResolvesWithoutSource() {
        harness.setLife(player2, 20);
        Permanent hunter = addCreatureReady(player1, new WitchHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hunter));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Witch Hunter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Bounce resolves after Witch Hunter leaves the battlefield")
    void bounceResolvesWithoutSource() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent target = addCreatureReady(player2, new Squire());
        addBounceMana(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hunter));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Witch Hunter");
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertInHand(player2, "Squire");
    }

    @Test
    @DisplayName("Bounce does not resolve if its controller gains control of the target")
    void bounceRechecksOpponentsControlAtResolution() {
        addCreatureReady(player1, new WitchHunter());
        Squire targetCard = new Squire();
        targetCard.setOwnerId(player2.getId());
        Permanent target = addCreatureReady(player2, targetCard);
        addBounceMana(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squire");
        harness.assertNotInHand(player1, "Squire");
        harness.assertNotInHand(player2, "Squire");
        assertThat(gd.stack).isEmpty();
    }

    private void addBounceMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
