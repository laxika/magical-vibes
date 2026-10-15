package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StewardOfSolidarity.class})
class StewardOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Ability creates a 1/1 white Warrior token with vigilance")
    void abilityCreatesWarriorToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfSolidarity());
        steward.setSummoningSick(false);

        int stewardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(steward);
        harness.activateAbility(player1, stewardIdx, 0, null, null);
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Warrior");
        assertThat(warrior.getCard().getPower()).isEqualTo(1);
        assertThat(warrior.getCard().getToughness()).isEqualTo(1);
        assertThat(warrior.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        assertThat(warrior.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(warrior.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Ability taps Steward and exerts it (won't untap next untap step)")
    void abilityTapsAndExertsSteward() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfSolidarity());
        steward.setSummoningSick(false);

        int stewardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(steward);
        harness.activateAbility(player1, stewardIdx, 0, null, null);
        assertThat(steward.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(steward.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Ability cannot be activated with summoning sickness")
    void abilityCannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new StewardOfSolidarity());
        // Steward has summoning sickness (default) — a {T} ability can't be activated.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exertIsPaidBeforeResolutionAndExpiresAfterOneUntapStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfSolidarity());
        steward.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(steward.isTapped()).isTrue();
        assertThat(steward.getSkipUntapCount()).isEqualTo(1);
        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Warrior")).hasSize(1);

        harness.performUntapStep(player2);
        assertThat(steward.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(steward.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(steward.isTapped()).isFalse();
    }

    @Test
    void tappedStewardCannotActivateAgain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfSolidarity());
        steward.setSummoningSick(false);
        steward.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }

    @Test
    void repeatedExertionsBeforeNextUntapExpireTogether() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new StewardOfSolidarity());
        steward.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        steward.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior")).hasSize(2);
        harness.performUntapStep(player1);
        assertThat(steward.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(steward.isTapped()).isFalse();
    }
}
