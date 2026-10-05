package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArachnusSpinner;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.d.DwarvenLieutenant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirkwood.class, GrizzlyBears.class, ArachnusSpinner.class, HowlpackWolf.class,
        DwarvenLieutenant.class, ArtificialEvolution.class, BoggartShenanigans.class})
class MirkwoodTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Permanent mirkwood = harness.enterBattlefieldAndReturn(player1, new Mirkwood());

        assertThat(mirkwood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or green mana")
    void manaAbilityAddsChosenMana() {
        Permanent mirkwood = addReadyMirkwood();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mirkwood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability puts two counters on a Bear")
    void sacrificeAbilityBoostsBear() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();

        harness.activateAbility(player1, battlefieldIndex(mirkwood), 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirkwood);
        harness.assertInGraveyard(player1, "Mirkwood");
    }

    @Test
    @DisplayName("Sacrifice ability also targets Spiders and Wolves")
    void sacrificeAbilityBoostsSpiderAndWolf() {
        Permanent firstMirkwood = addReadyMirkwood();
        Permanent secondMirkwood = addReadyMirkwood();
        Permanent spider = addCreatureReady(player1, new ArachnusSpinner());
        Permanent wolf = addCreatureReady(player1, new HowlpackWolf());
        addCounterAbilityMana(2);
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(firstMirkwood), 1, null, spider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(secondMirkwood), 1, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target another creature type")
    void sacrificeAbilityRejectsOtherCreature() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent dwarf = addCreatureReady(player1, new DwarvenLieutenant());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Bear, Spider, or Wolf");
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityIsSorcerySpeedOnly() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaAbilityAddsBlackImmediately() {
        Permanent mirkwood = addReadyMirkwood();

        harness.activateAbility(player1, battlefieldIndex(mirkwood), 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(mirkwood.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndPaysManaBeforeCountersResolve() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(mirkwood), 1, null, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirkwood);
        harness.assertInGraveyard(player1, "Mirkwood");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsBear() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        addCounterAbilityMana();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mirkwood);
        assertThat(mirkwood.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent mirkwood = harness.enterBattlefieldAndReturn(player1, new Mirkwood());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mirkwood);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithAnAbilityOnTheStack() {
        Permanent firstMirkwood = addReadyMirkwood();
        Permanent secondMirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana(2);
        readyMainPhase();
        harness.activateAbility(player1, battlefieldIndex(firstMirkwood), 1, null, bear.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(secondMirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondMirkwood);
        assertThat(secondMirkwood.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canPutCountersOnANoncreatureKindredBear() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player1, "BEAR");
        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.isCreature(gd, enchantment)).isFalse();
        addCounterAbilityMana();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(mirkwood), 1, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mirkwood");
    }

    @Test
    void countersAreNotPlacedIfTargetLosesItsEligibleSubtype() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        readyMainPhase();
        harness.activateAbility(player1, battlefieldIndex(mirkwood), 1, null, bear.getId());
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.handleListChoice(player1, "ELF");
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.BEAR)).isFalse();

        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Mirkwood");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        Permanent mirkwood = addReadyMirkwood();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mirkwood);
        assertThat(mirkwood.isTapped()).isFalse();
    }

    private Permanent addReadyMirkwood() {
        Permanent mirkwood = harness.addToBattlefieldAndReturn(player1, new Mirkwood());
        mirkwood.untap();
        return mirkwood;
    }

    private void addCounterAbilityMana() {
        addCounterAbilityMana(1);
    }

    private void addCounterAbilityMana(int activations) {
        harness.addMana(player1, ManaColor.COLORLESS, 2 * activations);
        harness.addMana(player1, ManaColor.BLACK, activations);
        harness.addMana(player1, ManaColor.GREEN, activations);
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
