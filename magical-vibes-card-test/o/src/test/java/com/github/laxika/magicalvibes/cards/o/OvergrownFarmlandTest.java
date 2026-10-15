package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
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

@CardUsed({OvergrownFarmland.class, Mountain.class, YoungWolf.class})
class OvergrownFarmlandTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addBasicLand(player1);

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new YoungWolf());
        }

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped lands count toward entering untapped")
    void tappedLandsCount() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonbasic lands count toward entering untapped")
    void nonbasicLandsCount() {
        harness.addToBattlefield(player1, new OvergrownFarmland());
        harness.addToBattlefield(player1, new OvergrownFarmland());

        playFarmland();

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isFalse();
    }

    @Test
    @DisplayName("One land and nonland permanents still cause tapped entry")
    void oneLandAndNonlandsEnterTapped() {
        addBasicLand(player1);
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addToBattlefield(player1, new YoungWolf());

        playFarmland();

        assertThat(findFarmland(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addFarmlandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addFarmlandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playFarmland() {
        harness.setHand(player1, List.of(new OvergrownFarmland()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
    }

    private void addFarmlandReady(Player player) {
        addCreatureReady(player, new OvergrownFarmland());
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findFarmland(Player player) {
        return findPermanent(player, "Overgrown Farmland");
    }
}
