package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SoddenVerdure.class, Mountain.class})
class SoddenVerdureTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than two basic lands")
    void entersTappedWithFewerThanTwoBasicLands() {
        addBasicLand(player1);

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with at least two basic lands")
    void entersUntappedWithAtLeastTwoBasicLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the controller's basic lands count")
    void onlyControllersBasicLandsCount() {
        addBasicLand(player2);
        addBasicLand(player2);

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonbasic lands do not count toward the requirement")
    void nonbasicLandsDoNotCount() {
        addNonbasicLand(player1);
        addNonbasicLand(player1);

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenManaProducesMana() {
        addReadySoddenVerdure(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueManaProducesMana() {
        addReadySoddenVerdure(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playSoddenVerdure() {
        harness.setHand(player1, List.of(new SoddenVerdure()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
    }

    private void addReadySoddenVerdure(Player player) {
        Permanent permanent = new Permanent(new SoddenVerdure());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void addBasicLand(Player player) {
        Card land = new Mountain();
        gd.playerBattlefields.get(player.getId()).add(new Permanent(land));
    }

    private void addNonbasicLand(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new SoddenVerdure()));
    }

    private Permanent findSoddenVerdure(Player player) {
        return findPermanent(player, "Sodden Verdure");
    }
}
