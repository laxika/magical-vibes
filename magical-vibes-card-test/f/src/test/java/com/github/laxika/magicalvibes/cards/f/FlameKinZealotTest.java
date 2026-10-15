package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameKinZealot.class, Watchwolf.class})
@DisplayName("Flame-Kin Zealot")
class FlameKinZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Entering boosts and gives haste to creatures you control, including itself")
    void boostsAndGivesHasteToOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Watchwolf());

        castZealot();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();

        Permanent zealot = findPermanent(player1, "Flame-Kin Zealot");
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.HASTE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect creatures that enter later")
    void doesNotAffectCreaturesEnteringLater() {
        castZealot();

        harness.castFromHand(player1, new Watchwolf(), "{G}{W}");
        resolveAllTriggers();

        Permanent laterCreature = findPermanent(player1, "Watchwolf");
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The boost and haste expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());

        castZealot();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive both effects")
    void affectsCreaturesPresentAtResolution() {
        harness.enterBattlefieldAndReturn(player1, new FlameKinZealot());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new Watchwolf());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Multiple entrance triggers stack their boosts")
    void multipleTriggersStackBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());

        castZealot();
        castZealot();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    private void castZealot() {
        harness.castFromHand(player1, new FlameKinZealot(), "{1}{R}{R}{W}");
        resolveAllTriggers();
    }
}
