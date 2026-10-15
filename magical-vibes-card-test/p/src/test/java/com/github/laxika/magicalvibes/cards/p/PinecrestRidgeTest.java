package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DreamLeash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PinecrestRidge.class, PsychicPuppetry.class, DreamLeash.class})
class PinecrestRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapsForColorlessAndUntapsNormally() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for red adds {R} and the land stays tapped through the next untap step")
    void tapsForRedAndSkipsNextUntap() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(ridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green adds {G} and the land untaps again on the following turn")
    void tapsForGreenAndSkipsOnlyOneUntapStep() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colored mana resolves immediately without using the stack")
    void coloredManaResolvesImmediately() {
        Permanent ridge = addRidge();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(ridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untap spell works and two colored activations skip only the same next untap step")
    void repeatedColoredActivationsSkipOnlyOneStep() {
        Permanent ridge = addRidge();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new PsychicPuppetry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, ridge.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(ridge.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isTrue();
        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Changing control does not prevent untapping during the new controller's next untap step")
    void newControllerCanUntapBeforeActivatorsNextStep() {
        Permanent ridge = addRidge();
        harness.activateAbility(player1, 0, 1, null, null);
        giveRidgeToOpponent(ridge);

        advanceToUpkeep(player2);

        assertThat(ridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The restriction expires during the activator's next untap step even after control changes")
    void restrictionExpiresDuringActivatorsStepAfterControlChange() {
        Permanent ridge = addRidge();
        harness.activateAbility(player1, 0, 2, null, null);
        giveRidgeToOpponent(ridge);

        advanceToUpkeep(player1);
        assertThat(ridge.isTapped()).isTrue();
        advanceToUpkeep(player2);

        assertThat(ridge.isTapped()).isFalse();
    }

    private void giveRidgeToOpponent(Permanent ridge) {
        harness.setHand(player2, List.of(new DreamLeash()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, ridge.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ridge);
    }

    private Permanent addRidge() {
        return harness.addToBattlefieldAndReturn(player1, new PinecrestRidge());
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
