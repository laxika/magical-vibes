package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlacksmithsSkill.class, FountainOfYouth.class, GrizzlyBears.class, Ornithopter.class, WrathOfGod.class})
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

    @Test
    @DisplayName("Can protect and boost an opponent's artifact creature")
    void protectsOpponentsArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAt(target);

        assertProtected(target);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Its controller can target the protected creature again and boosts accumulate")
    void repeatedCastsAccumulateBoosts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castAt(target);
        castAt(target);

        assertProtected(target);
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Granted hexproof prevents an opponent from targeting the permanent")
    void preventsOpponentTargeting() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAt(target);
        harness.setHand(player2, List.of(new BlacksmithsSkill()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Granted indestructible saves the target from a destroy-all spell")
    void survivesUntargetedDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unprotected = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAt(target);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(unprotected);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(unprotected.getCard());
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new BlacksmithsSkill()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void assertProtected(Permanent target) {
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
