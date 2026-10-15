package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.u.UnauthorizedExit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({FeloniousRage.class, NoviceInspector.class, UnauthorizedExit.class})
class FeloniousRageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature +2/+0 and haste until end of turn")
    void boostsCreatureAndGrantsHaste() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);

        assertThat(gqs.getEffectivePower(gd, inspector)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, inspector)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, inspector, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, inspector)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inspector, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creates a 2/2 white and blue Detective when the targeted creature dies this turn")
    void createsDetectiveWhenTargetDiesThisTurn() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);

        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Novice Inspector");
        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().isToken()).isTrue();
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getEffectivePower()).isEqualTo(2);
        assertThat(detective.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        harness.setHand(player1, List.of(new FeloniousRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, inspector.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each resolved Rage creates its own Detective when the creature dies")
    void multipleRagesCreateMultipleDetectives() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);
        castRage(inspector);

        assertThat(gqs.getEffectivePower(gd, inspector)).isEqualTo(5);
        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Detective")).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature dying on the next turn does not create a Detective")
    void delayedTriggerExpiresAtEndOfTurn() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Detective")).isZero();
    }

    @Test
    @DisplayName("A creature dying before Rage resolves does not create a Detective")
    void removedTargetMakesSpellFizzle() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        harness.setHand(player1, List.of(new FeloniousRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, inspector.getId());

        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Felonious Rage");
        assertThat(countPermanents(player1, "Detective")).isZero();
    }

    @Test
    @DisplayName("The death of another creature does not trigger Rage")
    void unrelatedCreatureDeathDoesNotCreateDetective() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(target);

        other.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Detective")).isZero();

        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Detective")).isEqualTo(1);
    }

    @Test
    @DisplayName("Rage can create a Detective when its target is itself a token")
    void tokenDeathCreatesDetective() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);
        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent originalToken = findPermanent(player1, "Detective");
        castRage(originalToken);
        originalToken.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Detective")).isEqualTo(1);
        assertThat(findPermanent(player1, "Detective").getId()).isNotEqualTo(originalToken.getId());
    }

    @Test
    @DisplayName("Death during the same turn's end step still creates a Detective")
    void deathDuringEndStepCreatesDetective() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);
        harness.forceStep(TurnStep.END_STEP);

        inspector.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Detective")).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning and recasting the targeted card does not carry over the death trigger")
    void bouncedAndRecastCreatureDoesNotCreateDetective() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        castRage(inspector);

        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, inspector.getId());
        harness.assertInHand(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Detective")).isZero();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Novice Inspector");
        returned.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Detective")).isZero();
    }

    private void castRage(Permanent target) {
        harness.setHand(player1, List.of(new FeloniousRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
