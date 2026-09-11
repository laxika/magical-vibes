package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RallyManeuver.class, GrizzlyBears.class, FountainOfYouth.class})
class RallyManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the first target +2/+0 and first strike, and the other target +0/+2 and lifelink")
    void affectsBothTargetsDifferently() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRallyManeuver(List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getToughnessModifier()).isZero();
        assertThat(firstTarget.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(firstTarget.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
        assertThat(secondTarget.getPowerModifier()).isZero();
        assertThat(secondTarget.getToughnessModifier()).isEqualTo(2);
        assertThat(secondTarget.getGrantedKeywords()).contains(Keyword.LIFELINK);
        assertThat(secondTarget.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Can resolve after choosing only the mandatory first target")
    void optionalSecondTargetCanBeDeclined() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRallyManeuver(List.of(firstTarget.getId()));

        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Temporary bonuses and keywords wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRallyManeuver(List.of(firstTarget.getId(), secondTarget.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firstTarget.getPowerModifier()).isZero();
        assertThat(firstTarget.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(secondTarget.getToughnessModifier()).isZero();
        assertThat(secondTarget.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Rejects a noncreature as the optional target")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RallyManeuver()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castRallyManeuver(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RallyManeuver()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
