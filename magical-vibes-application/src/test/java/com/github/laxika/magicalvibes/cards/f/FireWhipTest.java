package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireWhip.class, Squire.class, FlyingMen.class})
class FireWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(squire.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fire Whip");
    }

    @Test
    @DisplayName("Sacrificing the Aura deals 1 damage to a player and puts it in the graveyard")
    void sacrificeAbilityDealsDamage() {
        harness.setLife(player2, 20);

        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Fire Whip");
        harness.assertInGraveyard(player1, "Fire Whip");
        assertThat(squire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted ability kills a 1-toughness creature")
    void grantedAbilityKillsOneToughnessCreature() {
        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        Permanent flyingMen = addCreatureReady(player2, new FlyingMen());

        harness.activateAbility(player1, 0, null, flyingMen.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flying Men");
    }

    @Test
    @DisplayName("Creature loses the granted ability when Fire Whip leaves the battlefield")
    void abilityGoesAwayWhenAuraRemoved() {
        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Fire Whip can only enchant a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent enemySquire = harness.addToBattlefieldAndReturn(player2, new Squire());

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemySquire.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fire Whip can enchant a creature you control")
    void canEnchantOwnCreature() {
        Permanent squire = addCreatureReady(player1, new Squire());

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, squire.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fire Whip");
        assertThat(aura.getAttachedTo()).isEqualTo(squire.getId());
    }

    @Test
    @DisplayName("Sacrificing Fire Whip can deal 1 damage to a creature")
    void sacrificeAbilityDealsDamageToCreature() {
        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        Permanent flyingMen = addCreatureReady(player2, new FlyingMen());

        harness.activateAbility(player1, 1, null, flyingMen.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flying Men");
        harness.assertInGraveyard(player1, "Fire Whip");
    }

    @Test
    @DisplayName("A summoning-sick creature cannot use Fire Whip's granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped creature cannot activate Fire Whip's granted ability again")
    void tappedCreatureCannotActivateGrantedAbilityAgain() {
        Permanent squire = addCreatureReady(player1, new Squire());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(squire.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Fire Whip in response does not stop the creature's pending ability")
    void bothAbilitiesResolveAfterAuraIsSacrificed() {
        harness.setLife(player2, 20);
        Permanent squire = addCreatureReady(player1, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 1, null, player2.getId());

        harness.assertInGraveyard(player1, "Fire Whip");
        harness.assertNotOnBattlefield(player1, "Fire Whip");
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(squire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fire Whip can be sacrificed even when its enchanted creature is summoning sick")
    void sacrificeDoesNotRequireCreatureToBeReady() {
        harness.setLife(player2, 20);
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(squire.getId());

        harness.activateAbility(player1, 1, null, player2.getId());

        harness.assertInGraveyard(player1, "Fire Whip");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(squire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature's pending granted ability resolves after the creature and Aura leave")
    void grantedAbilityResolvesAfterSourceDies() {
        harness.setLife(player2, 20);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(flyingMen.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 1, null, flyingMen.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flying Men");
        harness.assertInGraveyard(player1, "Flying Men");
        harness.assertInGraveyard(player1, "Fire Whip");

        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
