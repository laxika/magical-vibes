package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.d.DiregrafHorde;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({FlareOfFaith.class, DawnhartRejuvenator.class, DiregrafHorde.class, Plains.class, FadingHope.class})
class FlareOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a Human +3/+3 and indestructible until end of turn")
    void givesHumanLargerBoostAndIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Gives a non-Human creature +2/+2 without indestructible")
    void givesNonHumanSmallerBoostWithoutIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DiregrafHorde());

        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        castResolve(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        prepareSpell();

        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can give an opponent's Human +3/+3 and indestructible")
    void canTargetOpponentsHuman() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());

        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A Human survives lethal damage with the granted indestructible")
    void humanSurvivesLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        castResolve(target);
        target.setMarkedDamage(7);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("A non-Human still dies to lethal damage after the boost")
    void nonHumanDiesToLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DiregrafHorde());

        castResolve(target);
        target.setMarkedDamage(6);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Does not apply either effect when the target is bounced in response")
    void doesNotAffectTargetThatLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new FadingHope()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(target.getCard());
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FlareOfFaith);
    }

    private void castResolve(Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FlareOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
