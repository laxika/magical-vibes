package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Stone Giant has activated ability with correct effects and target filter")
    void hasCorrectAbility() {
        StoneGiant card = new StoneGiant();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grantEffect = (GrantKeywordEffect) ability.getEffects().get(0);
        assertThat(grantEffect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.TARGET);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DestroyTargetPermanentAtEndStepEffect.class);
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
    }

    @Test
    @DisplayName("Grants flying to target creature you control with toughness less than its power")
    void grantsFlyingToTargetCreature() {
        Permanent stoneGiant = new Permanent(new StoneGiant());
        stoneGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stoneGiant);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent targetAfter = gqs.findPermanentById(gd, bears.getId());
        assertThat(targetAfter).isNotNull();
        assertThat(targetAfter.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Target creature is destroyed at the beginning of the next end step")
    void destroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent stoneGiant = new Permanent(new StoneGiant());
        stoneGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stoneGiant);

        Permanent elves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(elves);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        // Elves should still be on battlefield with flying
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gqs.findPermanentById(gd, elves.getId()).getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance to end step — elves should be destroyed (turn may auto-advance past end step)
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target creature with toughness equal to or greater than Stone Giant's power")
    void cannotTargetHighToughnessCreature() {
        Permanent stoneGiant = new Permanent(new StoneGiant());
        stoneGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stoneGiant);

        // Stone Giant is 3/4; another Stone Giant has toughness 4, which is not less than 3
        Permanent anotherGiant = new Permanent(new StoneGiant());
        anotherGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(anotherGiant);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, anotherGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's creatures")
    void cannotTargetOpponentCreatures() {
        Permanent stoneGiant = new Permanent(new StoneGiant());
        stoneGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stoneGiant);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stone Giant taps when ability is activated")
    void tapsWhenAbilityActivated() {
        Permanent stoneGiant = new Permanent(new StoneGiant());
        stoneGiant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stoneGiant);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(stoneGiant.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(stoneGiant.isTapped()).isTrue();
    }
}
