package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingBat.class})
class ScreechingBatTest extends BaseCardTest {

    /**
     * Both players have a way to make mana, so every "you may pay" in this class is asked and the test answers it with
     * the mana it adds (or without it); a payer with no way at all to pay isn't asked (CR 118.3, 608.2d).
     */
    @BeforeEach
    void everyPayerHasAManaSource() {
        giveManaSource(player1);
        giveManaSource(player2);
    }

    @Test
    @DisplayName("Transforms to Stalking Vampire when controller pays {2}{B}{B} during upkeep")
    void transformsWhenPayingMana() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        // Add mana after trigger resolves but before accepting (mana pools empty during step transitions)
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(bat.isTransformed()).isTrue();
        assertThat(bat.getCard().getName()).isEqualTo("Stalking Vampire");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when controller declines to pay")
    void doesNotTransformWhenDeclining() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stalking Vampire transforms back to Screeching Bat when controller pays during upkeep")
    void vampireTransformsBackWhenPayingMana() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());

        // Transform to Stalking Vampire first
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(bat.isTransformed()).isTrue();

        // Now pay to transform back
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());

        advanceToUpkeep(player2);

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
    }

    @Test
    @DisplayName("Transforming pays exactly two black and two generic mana")
    void transformationConsumesMana() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(bat.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Stalking Vampire stays transformed when its controller declines payment")
    void vampireDoesNotTransformWhenDeclining() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(bat.isTransformed()).isTrue();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bat.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Stalking Vampire");
    }

    @Test
    @DisplayName("Four mana with only one black mana cannot pay the transform cost")
    void cannotTransformWithoutTwoBlackMana() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bat.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Screeching Bat");
    }

    @Test
    @DisplayName("Stalking Vampire does not trigger during its opponent's upkeep")
    void vampireDoesNotTriggerDuringOpponentUpkeep() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new ScreechingBat());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(bat.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bat.isTransformed()).isTrue();
    }

}
