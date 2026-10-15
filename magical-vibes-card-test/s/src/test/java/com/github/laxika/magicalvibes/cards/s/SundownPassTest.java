package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SundownPass.class, Mountain.class, ScoldingAdministrator.class})
class SundownPassTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control many other lands")
    void entersUntappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new ScoldingAdministrator());
        }

        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pass = findPass(player1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addPassReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addPassReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonbasic tapped lands count toward the entry condition")
    void nonbasicTappedLandsCount() {
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new SundownPass()).tap();
        }
        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(2).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without a land play still checks other lands")
    void entryWithoutLandPlayChecksOtherLands() {
        addBasicLand(player1);
        addBasicLand(player2);
        addBasicLand(player2);

        Permanent first = harness.enterBattlefieldAndReturn(player1, new SundownPass());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new SundownPass());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A newly played untapped land can immediately produce mana without using the stack")
    void newlyPlayedLandCanProduceManaImmediately() {
        addBasicLand(player1);
        addBasicLand(player1);
        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 2, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(findPass(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering tapped prevents either mana ability from being activated")
    void cannotActivateManaAbilitiesAfterEnteringTapped() {
        harness.setHand(player1, List.of(new SundownPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPassReady(Player player) {
        return addCreatureReady(player, new SundownPass());
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findPass(Player player) {
        return findPermanent(player, "Sundown Pass");
    }
}
