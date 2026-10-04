package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlorifierOfDusk.class})
class GlorifierOfDuskTest extends BaseCardTest {

    // ===== Activated ability: pay 2 life for flying =====

    @Test
    @DisplayName("Paying 2 life grants flying until end of turn")
    void payLifeGrantsFlying() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    // ===== Activated ability: pay 2 life for vigilance =====

    @Test
    @DisplayName("Paying 2 life grants vigilance until end of turn")
    void payLifeGrantsVigilance() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    // ===== Both abilities together =====

    @Test
    @DisplayName("Can activate both abilities to gain flying and vigilance")
    void canActivateBothAbilities() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
    }

    // ===== Life payment validation =====

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Activation at exactly 2 life is accepted but player loses before ability resolves (CR 704.5a)")
    void canActivateAtExactlyTwoLife() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 2);

        // Activation is accepted (2 >= 2), life cost is paid, but SBAs fire immediately
        // and the player loses at 0 life before the ability resolves (CR 704.3 / 704.5a)
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Life is paid immediately while the keyword waits for resolution")
    void lifeIsPaidBeforeEitherAbilityResolves() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        harness.passBothPriorities();
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
        harness.passBothPriorities();
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Vigilance cannot be activated with insufficient life")
    void cannotActivateVigilanceWithInsufficientLife() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(glorifier.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void tappedSummoningSickCreatureCanActivateBothAbilities() {
        Permanent glorifier = harness.addToBattlefieldAndReturn(player1, new GlorifierOfDusk());
        glorifier.setSummoningSick(true);
        glorifier.setTapped(true);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
        assertThat(glorifier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Abilities grant keywords only to the activating creature")
    void grantsKeywordsOnlyToSource() {
        Permanent glorifier = addCreatureReady(player1, new GlorifierOfDusk());
        Permanent ally = addCreatureReady(player1, new GlorifierOfDusk());
        Permanent opponent = addCreatureReady(player2, new GlorifierOfDusk());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(glorifier.getGrantedKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
        assertThat(ally.getGrantedKeywords()).doesNotContain(Keyword.FLYING, Keyword.VIGILANCE);
        assertThat(opponent.getGrantedKeywords()).doesNotContain(Keyword.FLYING, Keyword.VIGILANCE);
    }
}
