package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuskLegionDreadnoughtTest extends BaseCardTest {

    // ===== Card effect configuration =====

    @Test
    @DisplayName("Has Crew 2 activated ability with CrewCost and AnimateSelfAsCreatureEffect")
    void hasCrewAbility() {
        DuskLegionDreadnought card = new DuskLegionDreadnought();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CrewCost.class);
        assertThat(((CrewCost) ability.getEffects().get(0)).requiredPower()).isEqualTo(2);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateSelfAsCreatureEffect.class);
    }

    // ===== Crew mechanic =====

    @Test
    @DisplayName("Dreadnought is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent dreadnought = addDreadnoughtReady(player1);

        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
        assertThat(dreadnought.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 2 animates Dreadnought")
    void crewWithSufficientPower() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears()); // 2/2, power >= 2

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadnought)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dreadnought)).isEqualTo(6);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addDreadnoughtReady(player1);
        // No creatures at all
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadnought.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addDreadnoughtReady(Player player) {
        Permanent perm = new Permanent(new DuskLegionDreadnought());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
