package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlacksmithsSkill.class, FountainOfYouth.class, GrizzlyBears.class, Ornithopter.class})
class BlacksmithsSkillTest extends BaseCardTest {

    @Test
    @DisplayName("Protects a regular creature without boosting it")
    void protectsRegularCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAt(target);

        assertProtected(target);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Protects and boosts an artifact creature")
    void boostsArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castAt(target);

        assertProtected(target);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Protects a noncreature permanent without boosting it")
    void protectsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castAt(target);

        assertProtected(target);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Protection and the boost wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castAt(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new BlacksmithsSkill()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void assertProtected(Permanent target) {
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
