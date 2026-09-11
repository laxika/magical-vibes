package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArachnusSpinner;
import com.github.laxika.magicalvibes.cards.d.DwarvenLieutenant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirkwood.class, GrizzlyBears.class, ArachnusSpinner.class, HowlpackWolf.class, DwarvenLieutenant.class})
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
        Permanent bear = addReadyPermanent(player1, new GrizzlyBears());
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
        Permanent spider = addReadyPermanent(player1, new ArachnusSpinner());
        Permanent wolf = addReadyPermanent(player1, new HowlpackWolf());
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
        Permanent dwarf = addReadyPermanent(player1, new DwarvenLieutenant());
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
        Permanent bear = addReadyPermanent(player1, new GrizzlyBears());
        addCounterAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mirkwood), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMirkwood() {
        Permanent mirkwood = addReadyPermanent(player1, new Mirkwood());
        mirkwood.untap();
        return mirkwood;
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
