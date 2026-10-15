package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WreckingCrew;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostileTakeover.class, GrizzlyBears.class, HillGiant.class, ColossalDreadmaw.class,
        WreckingCrew.class})
class HostileTakeoverTest extends BaseCardTest {

    @Test
    @DisplayName("Sets the two targets' base stats before dealing 3 damage to each creature")
    void setsTargetsAndDealsMassDamage() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent untouched = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        cast(List.of(weakened.getId(), strengthened.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, strengthened)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, strengthened)).isEqualTo(4);
        assertThat(strengthened.getMarkedDamage()).isEqualTo(3);
        assertThat(untouched.getMarkedDamage()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, strengthened)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, strengthened)).isEqualTo(3);
        assertThat(strengthened.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can choose no targets and still deal 3 damage to each creature")
    void canChooseNoTargets() {
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent creatureThatSurvives = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        cast(List.of());

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(creatureThatSurvives.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Requires the optional targets to be different creatures")
    void rejectsSameCreatureAsBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing only the first target makes it a 1/1 before the damage")
    void canChooseOnlyFirstTarget() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new WreckingCrew());

        cast(List.of(weakened.getId()));

        harness.assertInGraveyard(player2, "Wrecking Crew");
        harness.assertNotOnBattlefield(player2, "Wrecking Crew");
    }

    @Test
    @DisplayName("A single primary target must not also receive the second target's 4/4 effect")
    void singlePrimaryTargetReceivesOnlyFirstEffect() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new WreckingCrew());
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, weakened.getId());

        harness.assertInGraveyard(player2, "Wrecking Crew");
        harness.assertNotOnBattlefield(player2, "Wrecking Crew");
    }

    @Test
    @DisplayName("Does not deal mass damage when its only chosen target leaves the battlefield")
    void doesNotResolveWhenOnlyTargetIsIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WreckingCrew());
        Permanent untouched = harness.addToBattlefieldAndReturn(player2, new WreckingCrew());
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(untouched.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Hostile Takeover");
    }

    @Test
    @DisplayName("Still strengthens the second target and deals damage when the first target leaves")
    void resolvesWithOnlySecondTargetStillLegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WreckingCrew());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WreckingCrew());
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerHands.get(player1.getId()).add(first.getCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wrecking Crew");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(second.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Base stat changes leave +1/+1 counters in effect")
    void countersApplyAfterBaseStatsAreSet() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new WreckingCrew());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player2, new WreckingCrew());
        weakened.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        strengthened.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(List.of(weakened.getId(), strengthened.getId()));

        harness.assertOnBattlefield(player1, "Wrecking Crew");
        assertThat(gqs.getEffectivePower(gd, weakened)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, weakened)).isEqualTo(4);
        assertThat(weakened.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, strengthened)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strengthened)).isEqualTo(5);
        assertThat(strengthened.getMarkedDamage()).isEqualTo(3);
    }

    private void cast(List<UUID> targetIds) {
        if (targetIds.isEmpty()) {
            harness.castFromHand(player1, new HostileTakeover(), "{2}{U}{B}{R}");
            harness.passBothPriorities();
            return;
        }
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
