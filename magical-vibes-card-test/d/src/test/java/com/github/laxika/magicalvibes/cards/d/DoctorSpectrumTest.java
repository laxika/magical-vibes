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
    void countersExcludeOpposingHeroesAndNonHeroes() {
        Permanent ownHero = harness.addToBattlefieldAndReturn(player1, new BuckyBarnesEagerAlly());
        Permanent opposingHero = harness.addToBattlefieldAndReturn(player2, new BuckyBarnesEagerAlly());
        Permanent nonHero = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDoctorSpectrum(1);

        assertThat(ownHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterModeCanResolveWithoutOtherHeroes() {
        castDoctorSpectrum(1);

        assertThat(findPermanent(player1, "Doctor Spectrum").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void canDestroyYourOwnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        castDoctorSpectrum(2, enchantment.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment.getCard());
    }

    @Test
    void enchantmentModeDoesNothingIfTargetLeavesBeforeResolution() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DoctorSpectrum()));
        addDoctorMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseModeAndTarget(2, enchantment.getId());

        gd.playerBattlefields.get(player2.getId()).remove(enchantment);
        gd.playerHands.get(player2.getId()).add(enchantment.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(enchantment.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(enchantment.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void choosesModeWhenEnterTriggerIsPutOnStack() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new BuckyBarnesEagerAlly());
        harness.castFromHand(player1, new DoctorSpectrum(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, "Put a +1/+1 counter on each other Hero you control");
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Doctor Spectrum").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private void castDoctorSpectrum(int mode) {
        castDoctorSpectrum(mode, null);
    }

    private void castDoctorSpectrum(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new DoctorSpectrum()));
        addDoctorMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseModeAndTarget(mode, targetId);
        harness.passBothPriorities();
    }

    private void chooseModeAndTarget(int mode, UUID targetId) {
        String label = switch (mode) {
            case 0 -> "Create a 0/4 colorless Wall creature token with defender";
            case 1 -> "Put a +1/+1 counter on each other Hero you control";
            case 2 -> "Destroy target enchantment";
            default -> throw new IllegalArgumentException("Unknown Doctor Spectrum mode");
        };
        harness.handleListChoice(player1, label);
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    private void addDoctorMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
