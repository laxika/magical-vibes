package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSporesinger;
import com.github.laxika.magicalvibes.cards.f.FurnaceStrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ribskiff.class, AdaptiveSporesinger.class, FurnaceStrider.class})
class RibskiffTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new Ribskiff()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    void crewAnimatesRibskiffAndTapsCrew() {
        Permanent ribskiff = addCreatureReady(player1, new Ribskiff());
        Permanent crew = addCreatureReady(player1, new FurnaceStrider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ribskiff.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ribskiff)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void toxicDealsTwoPoisonCountersOnCombatDamage() {
        Permanent ribskiff = addCreatureReady(player1, new Ribskiff());
        addCreatureReady(player1, new FurnaceStrider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        ribskiff.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new Ribskiff());
        addCreatureReady(player1, new AdaptiveSporesinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void toxicAppliesDuringDamageWithoutUsingTheStack() {
        Permanent ribskiff = addCreatureReady(player1, new Ribskiff());
        addCreatureReady(player1, new FurnaceStrider());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        ribskiff.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreaturesCanCombinePowerToCrew() {
        Permanent ribskiff = addCreatureReady(player1, new Ribskiff());
        Permanent first = addCreatureReady(player1, new AdaptiveSporesinger());
        Permanent second = addCreatureReady(player1, new AdaptiveSporesinger());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ribskiff)).isTrue();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        addCreatureReady(player1, new Ribskiff());
        Permanent crew = addCreatureReady(player1, new FurnaceStrider());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }
}
