package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.BoundInSilence;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffinCanyon.class, DarajaGriffin.class, HulkingCyclops.class,
        ArtificialEvolution.class, BoundInSilence.class})
class GriffinCanyonTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new GriffinCanyon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps target Griffin and gives it +1/+1 until end of turn")
    void untapsAndBoostsGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
        griffin.tap();

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isFalse();
        assertThat(griffin.getPowerModifier()).isEqualTo(1);
        assertThat(griffin.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targets an untapped Griffin an opponent controls and still gives it +1/+1")
    void boostsUntappedOpponentGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new DarajaGriffin());

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isFalse();
        assertThat(griffin.getPowerModifier()).isEqualTo(1);
        assertThat(griffin.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Griffin Canyon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Griffin creature")
    void cannotTargetNonGriffin() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent nonGriffin = harness.addToBattlefieldAndReturn(player1, new HulkingCyclops());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonGriffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Griffin");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());

        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(griffin.getPowerModifier()).isEqualTo(0);
        assertThat(griffin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Untaps a noncreature Griffin without giving it a power/toughness bonus")
    void untapsNoncreatureGriffinWithoutBoosting() {
        harness.addToBattlefield(player1, new GriffinCanyon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
        harness.setHand(player1, List.of(new BoundInSilence(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Bound in Silence");
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handleListChoice(player1, "REBEL");
        harness.handleListChoice(player1, "GRIFFIN");
        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.GRIFFIN)).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        aura.tap();

        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.isTapped()).isFalse();
        assertThat(aura.getPowerModifier()).isZero();
        assertThat(aura.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A target that stops being a Griffin is neither untapped nor boosted")
    void targetLosingGriffinSubtypeDoesNotResolve() {
        Permanent canyon = harness.addToBattlefieldAndReturn(player1, new GriffinCanyon());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new DarajaGriffin());
        griffin.tap();
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, griffin.getId());
        assertThat(griffin.isTapped()).isTrue();
        assertThat(griffin.getPowerModifier()).isZero();

        harness.castAndResolveInstant(player1, 0, griffin.getId());
        harness.handleListChoice(player1, "GRIFFIN");
        harness.handleListChoice(player1, "BIRD");
        assertThat(gqs.hasEffectiveSubtype(gd, griffin, CardSubtype.GRIFFIN)).isFalse();
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isTrue();
        assertThat(griffin.getPowerModifier()).isZero();
        assertThat(griffin.getToughnessModifier()).isZero();
        assertThat(canyon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
