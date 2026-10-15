package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubyDaringTracker.class, Juggernaut.class, GiantGrowth.class, HerosDownfall.class,
        ConsulateDreadnought.class})
class RubyDaringTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a boost without a creature with power 4 or greater")
    void doesNotBoostWithoutLargeCreature() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isZero();
        assertThat(ruby.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while you control a creature with power 4 or greater")
    void boostsWithLargeCreature() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        harness.addToBattlefield(player1, new Juggernaut());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isEqualTo(2);
        assertThat(ruby.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(ruby.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ruby.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack boost still resolves after the qualifying creature is destroyed")
    void boostsAfterQualifyingCreatureLeaves() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new Juggernaut());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, juggernaut.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(juggernaut);
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isEqualTo(2);
        assertThat(ruby.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An uncrewed Vehicle does not satisfy the attack condition")
    void uncrewedVehicleDoesNotQualify() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        harness.addToBattlefield(player1, new ConsulateDreadnought());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(ruby.getPowerModifier()).isZero();
        assertThat(ruby.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's large creature does not satisfy the attack condition")
    void opponentsCreatureDoesNotQualify() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        harness.addToBattlefield(player2, new Juggernaut());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isZero();
        assertThat(ruby.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Ruby herself qualifies at exactly four power")
    void rubyQualifiesAtExactlyFourPower() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        ruby.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isEqualTo(2);
        assertThat(ruby.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ruby at three power does not satisfy the attack condition")
    void rubyBelowFourPowerDoesNotQualify() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        ruby.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(ruby.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Reaching four power after attacking does not create an attack trigger")
    void growingAfterAttackDoesNotTrigger() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, ruby.getId());
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isEqualTo(3);
        assertThat(ruby.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ruby can tap for mana while summoning sick and the mana ability uses no stack")
    void hasteAllowsImmediateManaActivation() {
        Permanent ruby = harness.addToBattlefieldAndReturn(player1, new RubyDaringTracker());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(ruby.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ruby can attack while summoning sick and her boost expires at cleanup")
    void hasteAllowsImmediateAttackAndBoostExpires() {
        Permanent ruby = harness.addToBattlefieldAndReturn(player1, new RubyDaringTracker());
        harness.addToBattlefield(player1, new Juggernaut());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(ruby.isTapped()).isTrue();
        assertThat(ruby.getPowerModifier()).isEqualTo(2);
        assertThat(ruby.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ruby.getPowerModifier()).isZero();
        assertThat(ruby.getToughnessModifier()).isZero();
    }
}
