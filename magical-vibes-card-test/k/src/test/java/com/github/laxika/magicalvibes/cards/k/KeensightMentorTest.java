package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlertHeedbonder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeensightMentor.class, GrizzlyBears.class, AlertHeedbonder.class})
class KeensightMentorTest extends BaseCardTest {

    @Test
    void entersWithVigilanceCounterOnTargetNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KeensightMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void cannotTargetHumanCreature() {
        Permanent target = addCreatureReady(player1, new KeensightMentor());
        harness.setHand(player1, List.of(new KeensightMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityPutsCountersOnlyOnCreaturesWithVigilance() {
        Permanent mentor = addCreatureReady(player1, new KeensightMentor());
        Permanent vigilantCreature = addCreatureReady(player1, new GrizzlyBears());
        vigilantCreature.setCounterCount(CounterType.VIGILANCE, 1);
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vigilantCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOpponentsNonHumanCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KeensightMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutAnyLegalTarget() {
        harness.setHand(player1, List.of(new KeensightMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Keensight Mentor");
        assertThat(findPermanent(player1, "Keensight Mentor").getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enterTriggerDoesNotPutCounterOnTargetThatLeftBattlefield() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KeensightMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.VIGILANCE)).isZero();
        harness.assertOnBattlefield(player1, "Keensight Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityIncludesPrintedVigilanceAndHumanCreaturesButNotOpponents() {
        Permanent mentor = addCreatureReady(player1, new KeensightMentor());
        mentor.setCounterCount(CounterType.VIGILANCE, 1);
        Permanent vigilantHuman = addCreatureReady(player1, new AlertHeedbonder());
        Permanent opposingCreature = addCreatureReady(player2, new AlertHeedbonder());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mentor.isTapped()).isTrue();
        assertThat(vigilantHuman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vigilantHuman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityChecksVigilanceAtResolutionAndSurvivesSourceLeaving() {
        Permanent mentor = addCreatureReady(player1, new KeensightMentor());
        Permanent losingVigilance = addCreatureReady(player1, new GrizzlyBears());
        losingVigilance.setCounterCount(CounterType.VIGILANCE, 1);
        Permanent gainingVigilance = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        losingVigilance.setCounterCount(CounterType.VIGILANCE, 0);
        gainingVigilance.setCounterCount(CounterType.VIGILANCE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(mentor);
        gd.playerGraveyards.get(player1.getId()).add(mentor.getCard());
        harness.passBothPriorities();

        assertThat(losingVigilance.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gainingVigilance.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void summoningSickMentorCannotActivateTapAbility() {
        Permanent mentor = addCreatureReady(player1, new KeensightMentor());
        mentor.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedMentorCannotActivateTapAbility() {
        Permanent mentor = addCreatureReady(player1, new KeensightMentor());
        mentor.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
