package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishHero.class, GrayOgre.class})
class BenalishHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Can band with one non-banding attacker")
    void canBandWithOneNonBandingAttacker() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(hero.getBandId()).isNotNull();
        assertThat(hero.getBandId()).isEqualTo(nonBander.getBandId());
    }

    @Test
    @DisplayName("Can band multiple banding attackers with one non-banding attacker")
    void canBandMultipleBandingAttackersWithOneNonBandingAttacker() {
        Permanent hero = addCreatureReady(player1, new BenalishHero());
        Permanent secondHero = addCreatureReady(player1, new BenalishHero());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        declareBand(List.of(0, 1, 2), List.of(List.of(0, 1, 2)));

        assertThat(hero.getBandId()).isNotNull();
        assertThat(secondHero.getBandId()).isEqualTo(hero.getBandId());
        assertThat(nonBander.getBandId()).isEqualTo(hero.getBandId());
    }

    private void declareBand(List<Integer> attackerIndices, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, bands));
    }
}
