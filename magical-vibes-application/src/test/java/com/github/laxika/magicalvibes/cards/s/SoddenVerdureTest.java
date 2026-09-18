package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoddenVerdure.class, Forest.class, GrizzlyBears.class, Island.class, DrownedCatacomb.class})
class SoddenVerdureTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than two basic lands")
    void entersTappedWithFewerThanTwoBasicLands() {
        addBasicLand(player1);

        playVerdure(player1);

        assertThat(findVerdure(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with two basic lands")
    void entersUntappedWithTwoBasicLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        playVerdure(player1);

        assertThat(findVerdure(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonbasic lands and non-land permanents do not satisfy the check")
    void onlyBasicLandsSatisfyTheCheck() {
        harness.addToBattlefield(player1, new SoddenVerdure());
        harness.addToBattlefield(player1, new GrizzlyBears());

        playVerdure(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(2).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent verdure = addReadyVerdure(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verdure.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent verdure = addReadyVerdure(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verdure.isTapped()).isTrue();
    }

    private void playVerdure(Player player) {
        harness.setHand(player, List.of(new SoddenVerdure()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyVerdure(Player player) {
        Permanent verdure = harness.addToBattlefieldAndReturn(player, new SoddenVerdure());
        verdure.setSummoningSick(false);
        return verdure;
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Forest());
    }

    private Permanent findVerdure(Player player) {
        return findPermanent(player, "Sodden Verdure");
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        playSoddenVerdure();

        assertThat(findSoddenVerdure(player1).isTapped()).isTrue();
    }

    private void playSoddenVerdure() {
        harness.setHand(player1, List.of(new SoddenVerdure()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySoddenVerdure(Player player) {
        Permanent permanent = new Permanent(new SoddenVerdure());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findSoddenVerdure(Player player) {
        return findPermanent(player, "Sodden Verdure");
    }
}
