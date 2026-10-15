package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AIMBot;
import com.github.laxika.magicalvibes.cards.x.X23DeadlyWeapon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZardaThePowerPrincess.class, ZuriWarriorOfWakanda.class, GrizzlyBears.class,
        AIMBot.class, X23DeadlyWeapon.class})
class ZardaThePowerPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("A Hero attacking alone gets +1/+1")
    void loneHeroGetsExaltedBonus() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent ownHero = addCreatureReady(player1, new ZuriWarriorOfWakanda());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(3);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Hero creatures do not gain exalted")
    void excludesNonHeroes() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroes controlled by an opponent do not gain exalted")
    void excludesOpponentsHeroes() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent opposingHero = addCreatureReady(player2, new ZuriWarriorOfWakanda());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingHero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zarda does not grant exalted to itself or non-Hero creatures")
    void excludesSourceAndNonHeroes() {
        Permanent zarda = addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zarda)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zarda)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void nonHeroAttackingAloneGetsBonusFromWaitingHero() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent hero = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent attacker = addCreatureReady(player1, new AIMBot());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
    }

    @Test
    void zardaAttackingAloneGetsBonusFromOtherHero() {
        Permanent zarda = addCreatureReady(player1, new ZardaThePowerPrincess());
        addCreatureReady(player1, new ZuriWarriorOfWakanda());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zarda)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zarda)).isEqualTo(5);
    }

    @Test
    void eachOtherHeroAddsItsOwnExaltedBonus() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        addCreatureReady(player1, new ZuriWarriorOfWakanda());
        addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent attacker = addCreatureReady(player1, new AIMBot());

        declareAttackers(player1, List.of(3));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    void multipleAttackersDoNotTriggerExalted() {
        Permanent zarda = addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent hero = addCreatureReady(player1, new ZuriWarriorOfWakanda());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, zarda)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zarda)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
    }

    @Test
    void removingZardaDoesNotRemoveAlreadyTriggeredBonus() {
        Permanent zarda = addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent hero = addCreatureReady(player1, new ZuriWarriorOfWakanda());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(zarda);
        gd.playerGraveyards.get(player1.getId()).add(zarda.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
    }
}
