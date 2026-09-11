package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopyVista.class, Forest.class, Plains.class, DrownedCatacomb.class})
class CanopyVistaTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesGreenMana() {
        addReadyCanopyVista(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappingProducesWhiteMana() {
        addReadyCanopyVista(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playCanopyVista() {
        harness.setHand(player1, List.of(new CanopyVista()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCanopyVista(Player player) {
        Permanent permanent = new Permanent(new CanopyVista());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findCanopyVista(Player player) {
        return findPermanent(player, "Canopy Vista");
    }
}
