package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JorubaiMurkLurker.class, Forest.class, Swamp.class, RuneclawBear.class})
class JorubaiMurkLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/3 without a Swamp")
    void noBoostWithoutSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player1, new Forest());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Swamp")
    void boostWithSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player1, new Swamp());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's Swamp does not grant the boost")
    void noBoostFromOpponentSwamp() {
        harness.addToBattlefield(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player2, new Swamp());

        Permanent lurker = findPermanent(player1, "Jorubai Murk Lurker");
        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(3);
    }

    @Test
    @DisplayName("{1}{B} grants target creature lifelink until end of turn")
    void activatedAbilityGrantsLifelinkToTargetCreature() {
        addCreatureReady(player1, new JorubaiMurkLurker());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new JorubaiMurkLurker());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Multiple Swamps grant only one +1/+1 boost")
    void multipleSwampsDoNotMultiplyBoost() {
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new JorubaiMurkLurker());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, lurker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lurker)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Murk Lurker can grant itself lifelink without a Swamp")
    void canActivateWhileTappedAndSummoningSickTargetingSelf() {
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new JorubaiMurkLurker());
        lurker.setSummoningSick(true);
        lurker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, lurker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lurker, Keyword.LIFELINK)).isTrue();
        assertThat(lurker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lifelink resolves even if the Murk Lurker leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent lurker = addCreatureReady(player1, new JorubaiMurkLurker());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lurker);
        gd.playerGraveyards.get(player1.getId()).add(lurker.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }
}
