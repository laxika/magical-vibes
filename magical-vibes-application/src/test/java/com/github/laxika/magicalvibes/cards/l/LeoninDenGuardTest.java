package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fireshrieker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninDenGuard.class, Fireshrieker.class})
class LeoninDenGuardTest extends BaseCardTest {

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
