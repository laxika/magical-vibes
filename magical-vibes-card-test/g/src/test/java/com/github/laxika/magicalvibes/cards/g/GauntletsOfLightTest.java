package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BallLightning;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GauntletsOfLight.class, BallLightning.class, FountainOfYouth.class, GoblinPiker.class})
class GauntletsOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gauntlets of Light gives the enchanted creature +0/+2 and toughness-based combat damage")
    void enchantedCreatureGetsBoostAndUsesToughnessForCombatDamage() {
        Permanent ballLightning = addCreatureReady(player1, new BallLightning());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GauntletsOfLight());
        aura.setAttachedTo(ballLightning.getId());

        assertThat(gqs.getEffectivePower(gd, ballLightning)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ballLightning)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, ballLightning)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature can pay {2}{W} to untap itself")
    void enchantedCreatureCanUntapItself() {
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        piker.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GauntletsOfLight());
        aura.setAttachedTo(piker.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(piker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Gauntlets of Light removes its granted effects")
    void effectsStopWhenAuraIsRemoved() {
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GauntletsOfLight());
        aura.setAttachedTo(piker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, piker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Gauntlets of Light cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GauntletsOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted attacker deals toughness-based damage even when its power is higher")
    void attackerDealsToughnessBasedDamage() {
        Permanent attacker = addCreatureReady(player1, new BallLightning());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GauntletsOfLight());
        aura.setAttachedTo(attacker.getId());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent's enchanted creature receives the boost and can activate the untap ability")
    void opponentControlsGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        creature.tap();
        harness.setHand(player1, List.of(new GauntletsOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gauntlets of Light").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap activation still resolves after the Aura leaves the battlefield")
    void untapResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GauntletsOfLight());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
