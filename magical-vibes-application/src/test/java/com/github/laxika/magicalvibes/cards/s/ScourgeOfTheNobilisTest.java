package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.k.KithkinZealot;
import com.github.laxika.magicalvibes.cards.w.WardOfBones;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.h.HeartlashCinder;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BattlegateMimic;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScourgeOfTheNobilis.class, HeartlashCinder.class, KithkinZealot.class, NettleSentinel.class, WardOfBones.class, BattlegateMimic.class})
class ScourgeOfTheNobilisTest extends BaseCardTest {

    private Permanent attachTo(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ScourgeOfTheNobilis());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Red enchanted creature gets +1/+1")
    void redGetsBoost() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HeartlashCinder());
        attachTo(giant);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Red enchanted creature can pay {R/W} to pump itself +1/+0")
    void redGrantsFirebreathing() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HeartlashCinder());
        attachTo(giant);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 1 base +1 (red boost) +1 (pump) = 3
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
    }

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and lifelink")
    void whiteGetsBoostAndLifelink() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new KithkinZealot());
        attachTo(vanguard);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Non-red non-white enchanted creature gets no boost or lifelink")
    void offColorGetsNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        attachTo(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Boost and lifelink wear off when the aura is removed")
    void wearsOffWhenRemoved() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new KithkinZealot());
        Permanent aura = attachTo(vanguard);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Scourge of the Nobilis")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new NettleSentinel());
        harness.addToBattlefield(player1, new WardOfBones());
        harness.setHand(player1, List.of(new ScourgeOfTheNobilis()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = findPermanent(player1, "Ward of Bones");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void redAndWhiteCreatureGetsBothBonusesAndCanPayWhiteRepeatedly() {
        Permanent mimic = harness.addToBattlefieldAndReturn(player1, new BattlegateMimic());
        attachTo(mimic);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.LIFELINK)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
    }

    @Test
    void whiteOnlyCreatureCannotActivatePump() {
        harness.addToBattlefield(player1, new KithkinZealot());
        attachTo(findPermanent(player1, "Kithkin Zealot"));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void offColorCreatureCannotActivatePump() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        attachTo(sentinel);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedPumpResolvesAfterAuraLeaves() {
        Permanent cinder = harness.addToBattlefieldAndReturn(player1, new HeartlashCinder());
        Permanent aura = attachTo(cinder);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cinder)).isEqualTo(1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opposingCreatureControllerCanActivateGrantedAbilityAndGainsLife() {
        Permanent mimic = harness.addToBattlefieldAndReturn(player2, new BattlegateMimic());
        attachTo(mimic);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        gd.currentStep = TurnStep.COMBAT_DAMAGE;
        mimic.setAttacking(true);
        mimic.setAttackTarget(player1.getId());

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(25);
    }

    @Test
    void auraCanBeCastOnOpposingCreature() {
        Permanent cinder = harness.addToBattlefieldAndReturn(player2, new HeartlashCinder());
        harness.setHand(player1, List.of(new ScourgeOfTheNobilis()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, cinder.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Scourge of the Nobilis").getAttachedTo())
                .isEqualTo(cinder.getId());
        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cinder)).isEqualTo(2);
    }
}
