package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
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

@CardUsed({SinstrikersWill.class, GruulScrapper.class, GruulTurf.class})
class SinstrikersWillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sinstriker's Will attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GruulScrapper());
        harness.setHand(player1, List.of(new SinstrikersWill()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SinstrikersWill
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Sinstriker's Will cannot target a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GruulTurf());
        harness.setHand(player1, List.of(new SinstrikersWill()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature deals damage equal to its power to an attacking creature")
    void dealsPowerDamageToAttackingCreature() {
        Permanent source = addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GruulScrapper);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability deals exactly the enchanted creature's power")
    void dealsExactlySourcePowerDamage() {
        Permanent source = addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can target a blocking creature")
    void dealsPowerDamageToBlockingCreature() {
        addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setBlocking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GruulScrapper);
    }

    @Test
    @DisplayName("The granted ability cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    @Test
    @DisplayName("The granted ability stops resolving damage when the target stops attacking")
    void targetMustStillBeAttackingOrBlockingAtResolution() {
        Permanent source = addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature receives the granted ability")
    void doesNotGrantAbilityToOtherCreatures() {
        addAttachedAura();
        Permanent otherCreature = addCreatureReady(player1, new GruulScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchanted creature can use the granted ability")
    void opponentControlsEnchantedCreature() {
        Permanent source = addCreatureReady(player2, new GruulScrapper());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(source.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GruulScrapper());
        target.setAttacking(true);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GruulScrapper);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damage uses the enchanted creature's power at resolution")
    void usesPowerAtResolution() {
        Permanent source = addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick creature cannot activate the granted tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent source = addAttachedAura();
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        target.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted ability can target a friendly blocking creature")
    void canTargetFriendlyBlockingCreature() {
        Permanent source = addAttachedAura();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GruulScrapper());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setBlocking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.isTapped()).isTrue();
    }

    private Permanent addAttachedAura() {
        Permanent enchantedCreature = addCreatureReady(player1, new GruulScrapper());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(enchantedCreature.getId());
        return enchantedCreature;
    }
}
