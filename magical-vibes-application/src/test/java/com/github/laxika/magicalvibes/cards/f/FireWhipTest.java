package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.r.RazortoothRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireWhip.class, BenalishInfantry.class, RazortoothRats.class})
class FireWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(infantry.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fire Whip");
    }

    @Test
    @DisplayName("Sacrificing the Aura deals 1 damage to a player and puts it in the graveyard")
    void sacrificeAbilityDealsDamage() {
        harness.setLife(player2, 20);

        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Fire Whip");
        harness.assertInGraveyard(player1, "Fire Whip");
        assertThat(infantry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted ability kills a 1-toughness creature")
    void grantedAbilityKillsOneToughnessCreature() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        addCreatureReady(player2, new RazortoothRats());
        Permanent rats = findPermanent(player2, "Razortooth Rats");

        harness.activateAbility(player1, 0, null, rats.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razortooth Rats");
    }

    @Test
    @DisplayName("Creature loses the granted ability when Fire Whip leaves the battlefield")
    void abilityGoesAwayWhenAuraRemoved() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Fire Whip can only enchant a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent enemyInfantry = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemyInfantry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fire Whip can enchant a creature you control")
    void canEnchantOwnCreature() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, infantry.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fire Whip");
        assertThat(aura.getAttachedTo()).isEqualTo(infantry.getId());
    }

    @Test
    @DisplayName("Sacrificing Fire Whip can deal 1 damage to a creature")
    void sacrificeAbilityDealsDamageToCreature() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        Permanent rats = addCreatureReady(player2, new RazortoothRats());

        harness.activateAbility(player1, 1, null, rats.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razortooth Rats");
        harness.assertInGraveyard(player1, "Fire Whip");
    }

    @Test
    @DisplayName("A summoning-sick creature cannot use Fire Whip's granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(infantry.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
