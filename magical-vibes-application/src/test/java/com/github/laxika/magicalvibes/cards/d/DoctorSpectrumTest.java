package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BuckyBarnesEagerAlly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoctorSpectrum.class, BuckyBarnesEagerAlly.class, GloriousAnthem.class, GrizzlyBears.class})
class DoctorSpectrumTest extends BaseCardTest {

    @Test
    void createsWallToken() {
        castDoctorSpectrum(0);

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void putsCountersOnOtherHeroesYouControl() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new BuckyBarnesEagerAlly());
        castDoctorSpectrum(1);

        Permanent doctor = findPermanent(player1, "Doctor Spectrum");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castDoctorSpectrum(2, enchantment.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
    }

    @Test
    void rejectsNonEnchantmentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoctorSpectrum()));
        addDoctorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    private void castDoctorSpectrum(int mode) {
        castDoctorSpectrum(mode, null);
    }

    private void castDoctorSpectrum(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new DoctorSpectrum()));
        addDoctorMana();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addDoctorMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
