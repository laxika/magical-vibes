package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SleekSchoonerTest extends BaseCardTest {

    // ===== Card effect configuration =====

    @Test
    @DisplayName("Has Crew 1 activated ability with CrewCost and AnimateSelfAsCreatureEffect")
    void hasCrewAbility() {
        SleekSchooner card = new SleekSchooner();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CrewCost.class);
        assertThat(((CrewCost) ability.getEffects().get(0)).requiredPower()).isEqualTo(1);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateSelfAsCreatureEffect.class);
    }

    // ===== Crew mechanic =====

    @Test
    @DisplayName("Schooner is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent schooner = addSchoonerReady(player1);

        assertThat(gqs.isCreature(gd, schooner)).isFalse();
        assertThat(schooner.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 1 animates Schooner")
    void crewWithSufficientPower() {
        Permanent schooner = addSchoonerReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears()); // 2/2, power >= 1

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(schooner.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(3);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without any creatures")
    void cannotCrewWithoutCreatures() {
        addSchoonerReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent schooner = addSchoonerReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(schooner.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, schooner)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent schooner = addSchoonerReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(schooner.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addSchoonerReady(Player player) {
        Permanent perm = new Permanent(new SleekSchooner());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
