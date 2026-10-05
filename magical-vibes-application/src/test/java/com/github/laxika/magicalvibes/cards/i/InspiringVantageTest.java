package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiringVantage.class, Mountain.class, PropheticPrism.class})
class InspiringVantageTest extends BaseCardTest {

    @Test
    void entersUntappedWithNoOtherLands() {
        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithOneOtherLand() {
        addMountain(player1);

        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isFalse();
    }

    @Test
    void opponentLandsDoNotCount() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player2);
        addMountain(player2);
        addMountain(player2);

        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isFalse();
    }

    @Test
    void nonlandPermanentsDoNotCount() {
        addMountain(player1);
        addMountain(player1);
        harness.addToBattlefield(player1, new PropheticPrism());

        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isFalse();
    }

    @Test
    void tappedNonbasicLandsCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new InspiringVantage()).tap();
        }

        playInspiringVantage();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(3).isTapped()).isTrue();
    }

    @Test
    void cannotActivateManaAbilityWhileTapped() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);
        playInspiringVantage();

        assertThatThrownBy(() -> harness.activateAbility(player1, 3, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 3, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void entersUntappedWithTwoOtherLands() {
        addMountain(player1);
        addMountain(player1);

        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        playInspiringVantage();

        assertThat(findVantage(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesRedMana() {
        addReadyVantage(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappingProducesWhiteMana() {
        addReadyVantage(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playInspiringVantage() {
        harness.setHand(player1, List.of(new InspiringVantage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyVantage(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new InspiringVantage());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findVantage(Player player) {
        return findPermanent(player, "Inspiring Vantage");
    }
}
