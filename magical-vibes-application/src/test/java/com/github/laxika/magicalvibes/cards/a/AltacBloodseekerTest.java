package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({AltacBloodseeker.class, FleshToDust.class, RuneclawBear.class})
class AltacBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creature dying gives +2/+0, first strike and haste together")
    void pumpsWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new RuneclawBear());

        destroyBear();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(2);
        assertThat(bloodseeker.getToughnessModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE, Keyword.HASTE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Runeclaw Bear"));

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Boost and keywords last through the end step and wear off at cleanup")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new RuneclawBear());

        destroyBear();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(2);
        assertThat(bloodseeker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE, Keyword.HASTE);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(bloodseeker.getPowerModifier()).isEqualTo(0);
        assertThat(bloodseeker.getToughnessModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Each opponent creature death adds another +2/+0")
    void multipleDeathsAccumulateBoosts() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new RuneclawBear());
        destroyBear();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.addToBattlefield(player2, new RuneclawBear());
        destroyBear();
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        Permanent bloodseeker = findBloodseeker();
        assertThat(bloodseeker.getPowerModifier()).isEqualTo(4);
        assertThat(bloodseeker.getToughnessModifier()).isEqualTo(0);
        assertThat(bloodseeker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    @Test
    @DisplayName("A pending death trigger does not boost a replacement Bloodseeker")
    void pendingTriggerDoesNotAffectNewPermanent() {
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.addToBattlefield(player2, new RuneclawBear());
        destroyBear();

        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, findBloodseeker().getId());
        harness.assertInGraveyard(player1, "Altac Bloodseeker");
        harness.addToBattlefield(player1, new AltacBloodseeker());
        harness.passUntil(TurnStep.END_STEP);

        Permanent replacement = findBloodseeker();
        assertThat(replacement.getPowerModifier()).isEqualTo(0);
        assertThat(replacement.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.HASTE);
    }

    private void destroyBear() {
        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Runeclaw Bear"));
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    private Permanent findBloodseeker() {
        return findPermanent(player1, "Altac Bloodseeker");
    }
}
