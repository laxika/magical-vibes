package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fireshrieker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninDenGuard.class, Fireshrieker.class})
class LeoninDenGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Multiple Equipment grant only one bonus, retained until the last Equipment leaves")
    void multipleEquipmentDoNotMultiplyBonus() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        first.setAttachedTo(guard.getId());
        second.setAttachedTo(guard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipment controlled by an opponent still enables the bonus")
    void opponentControlledEquipmentEnablesBonus() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Fireshrieker());
        equipment.setAttachedTo(guard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Only the equipped Den-Guard attacks without tapping")
    void equippedGuardAttacksWithoutTapping() {
        Permanent equipped = addCreatureReady(player1, new LeoninDenGuard());
        Permanent unequipped = addCreatureReady(player1, new LeoninDenGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        equipment.setAttachedTo(equipped.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(equipped.isTapped()).isFalse();
        assertThat(unequipped.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unequipped Leonin Den-Guard has no bonus")
    void unequippedHasNoBonus() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipped Leonin Den-Guard gets +1/+1 and vigilance")
    void equippedGetsBonusAndVigilance() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        equipment.setAttachedTo(guard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Leonin Den-Guard loses its bonus when it is no longer equipped")
    void losesBonusWhenUnequipped() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        equipment.setAttachedTo(guard.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Leonin Den-Guard is only enhanced when it is the equipped creature")
    void equipmentAttachedToAnotherCreatureDoesNotEnhanceGuard() {
        Permanent guard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent otherGuard = addCreatureReady(player1, new LeoninDenGuard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Fireshrieker());
        equipment.setAttachedTo(otherGuard.getId());

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, otherGuard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherGuard)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherGuard, Keyword.VIGILANCE)).isTrue();
    }
}
