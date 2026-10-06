package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SanctifiedCharge.class, SungracePegasus.class, RuneclawBear.class})
class SanctifiedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own creatures and grants first strike to own white creatures")
    void boostsOwnCreaturesAndGrantsFirstStrikeToWhiteCreatures() {
        Permanent whiteCreature = addCreatureReady(player1, new SungracePegasus());
        Permanent nonWhiteCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());
        Permanent opponentWhiteCreature = addCreatureReady(player2, new SungracePegasus());

        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, nonWhiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nonWhiteCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nonWhiteCreature, Keyword.FIRST_STRIKE)).isFalse();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentWhiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentWhiteCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentWhiteCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The boost and first strike grant wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent whiteCreature = addCreatureReady(player1, new SungracePegasus());

        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, whiteCreature, Keyword.FIRST_STRIKE)).isFalse();
    }


    @Test
    @DisplayName("Only creatures present when the spell resolves are affected")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);

        Permanent presentAtResolution = addCreatureReady(player1, new SungracePegasus());
        harness.passBothPriorities();
        Permanent enteredAfterResolution = addCreatureReady(player1, new SungracePegasus());

        assertThat(gqs.getEffectivePower(gd, presentAtResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, presentAtResolution)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, presentAtResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enteredAfterResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enteredAfterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enteredAfterResolution, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can resolve with no creatures without affecting later creatures")
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new SanctifiedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);

        Permanent laterCreature = addCreatureReady(player1, new SungracePegasus());
        harness.assertInGraveyard(player1, "Sanctified Charge");
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

}
