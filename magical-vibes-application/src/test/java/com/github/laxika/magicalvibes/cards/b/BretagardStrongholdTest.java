package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BretagardStronghold.class, GrizzledOutrider.class})
class BretagardStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BretagardStronghold()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Bretagard Stronghold").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAddsGreenMana() {
        Permanent stronghold = addReady(player1, new BretagardStronghold());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Puts counters on and grants vigilance and lifelink to two creatures")
    void boostsTwoCreatures() {
        Permanent stronghold = addReady(player1, new BretagardStronghold());
        Permanent first = addReady(player1, new GrizzledOutrider());
        Permanent second = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        assertThat(stronghold.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Bretagard Stronghold");
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        addReady(player1, new BretagardStronghold());
        Permanent bear = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Ability can target only creatures you control")
    void cannotTargetOpponentCreature() {
        addReady(player1, new BretagardStronghold());
        Permanent opponentCreature = addReady(player2, new GrizzledOutrider());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is sorcery speed only")
    void sorcerySpeedOnly() {
        addReady(player1, new BretagardStronghold());
        Permanent bear = addReady(player1, new GrizzledOutrider());
        addManaForAbility();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        addReady(player1, new BretagardStronghold());
        Permanent bear = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zero targets are allowed and costs are paid before resolution")
    void zeroTargetsAllowed() {
        Permanent stronghold = addReady(player1, new BretagardStronghold());
        addManaForAbility();

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());

        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stronghold);
        harness.assertInGraveyard(player1, "Bretagard Stronghold");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same creature cannot be targeted twice")
    void duplicateTargetRejected() {
        Permanent stronghold = addReady(player1, new BretagardStronghold());
        Permanent creature = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stronghold);
        assertThat(stronghold.isTapped()).isFalse();
    }

    @Test
    @DisplayName("More than two targets are rejected")
    void threeTargetsRejected() {
        addReady(player1, new BretagardStronghold());
        Permanent first = addReady(player1, new GrizzledOutrider());
        Permanent second = addReady(player1, new GrizzledOutrider());
        Permanent third = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land you control is not a legal target")
    void noncreatureTargetRejected() {
        addReady(player1, new BretagardStronghold());
        Permanent land = addReady(player1, new BretagardStronghold());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Stronghold cannot activate its sacrifice ability")
    void tappedSourceCannotActivate() {
        Permanent stronghold = addReady(player1, new BretagardStronghold());
        stronghold.tap();
        Permanent creature = addReady(player1, new GrizzledOutrider());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stronghold);
    }

    @Test
    @DisplayName("Activation is forbidden during combat on your own turn")
    void cannotActivateOutsideMainPhase() {
        addReady(player1, new BretagardStronghold());
        Permanent creature = addReady(player1, new GrizzledOutrider());
        addManaForAbility();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that changes controller is skipped while the remaining target is boosted")
    void resolvesOnlyForStillControlledTarget() {
        addReady(player1, new BretagardStronghold());
        Permanent first = addReady(player1, new GrizzledOutrider());
        Permanent second = addReady(player1, new GrizzledOutrider());
        addManaForAbility();
        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("No effects apply when the only target changes controller")
    void allTargetsIllegal() {
        addReady(player1, new BretagardStronghold());
        Permanent creature = addReady(player1, new GrizzledOutrider());
        addManaForAbility();
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        harness.assertInGraveyard(player1, "Bretagard Stronghold");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires an empty stack even during your main phase")
    void cannotActivateWithAbilityOnStack() {
        addReady(player1, new BretagardStronghold());
        Permanent second = addReady(player1, new BretagardStronghold());
        addManaForAbility();
        addManaForAbility();
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
