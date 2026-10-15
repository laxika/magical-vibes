package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunspearShikari.class, AccordersShield.class})
class SunspearShikariTest extends BaseCardTest {

    @Test
    @DisplayName("Without equipment, does not have first strike or lifelink")
    void withoutEquipmentNoKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("With equipment attached, has first strike and lifelink")
    void withEquipmentHasKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equipment on another creature does not grant keywords to Shikari")
    void equipmentOnOtherCreatureDoesNotGrantKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        Permanent other = addCreatureReady(player1, new SunspearShikari());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(other.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unattached equipment on battlefield does not grant keywords")
    void unattachedEquipmentDoesNotGrantKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("After equipment is detached, loses first strike and lifelink")
    void afterEquipmentDetachedLosesKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();

        shield.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Opponent's equipment attached to Shikari still grants keywords")
    void opponentEquipmentGrantsKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        Permanent shield = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        shield.setAttachedTo(shikari.getId());

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Resolving equip immediately grants both keywords")
    void resolvingEquipGrantsKeywords() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 1, null, shikari.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Removing one of two equipment keeps both keywords until the last is removed")
    void multipleEquipmentKeepKeywordsUntilLastRemoved() {
        Permanent shikari = addCreatureReady(player1, new SunspearShikari());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        first.setAttachedTo(shikari.getId());
        second.setAttachedTo(shikari.getId());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, shikari, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shikari, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Equipped Shikari kills an unequipped Shikari before receiving combat damage and gains life")
    void firstStrikeKillsBlockerAndLifelinkGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SunspearShikari());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SunspearShikari());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Sunspear Shikari");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple equipment do not multiply lifelink or grant a second combat hit")
    void multipleEquipmentGainLifeOnlyOnceForUnblockedDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SunspearShikari());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        first.setAttachedTo(attacker.getId());
        second.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
