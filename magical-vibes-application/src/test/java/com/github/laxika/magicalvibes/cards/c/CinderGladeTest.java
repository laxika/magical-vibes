package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderGlade.class, Mountain.class, Forest.class, DrownedCatacomb.class})
class CinderGladeTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Mountain());

        playCinderGlade();

        assertThat(findCinderGlade(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        playCinderGlade();

        assertThat(findCinderGlade(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playCinderGlade();

        assertThat(findCinderGlade(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        playCinderGlade();

        assertThat(findCinderGlade(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesRedMana() {
        addReadyCinderGlade(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappingProducesGreenMana() {
        addReadyCinderGlade(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void playCinderGlade() {
        harness.setHand(player1, List.of(new CinderGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCinderGlade(Player player) {
        Permanent permanent = new Permanent(new CinderGlade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findCinderGlade(Player player) {
        return findPermanent(player, "Cinder Glade");
    }
}
