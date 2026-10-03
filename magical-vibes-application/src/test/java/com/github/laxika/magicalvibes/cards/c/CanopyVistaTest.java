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
    void entersTappedWithNoLands() {
        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoTappedForests() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithMoreThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        playCanopyVista();

        assertThat(findCanopyVista(player1).isTapped()).isFalse();
    }

    @Test
    void basicLandTypesOnNonbasicLandDoNotCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CanopyVista());

        playCanopyVista();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(2).isTapped()).isTrue();
    }

    @Test
    void entersTappedWhenPutOntoBattlefieldWithoutBeingPlayed() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new CanopyVista());

        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    void entersUntappedWhenPutOntoBattlefieldWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new CanopyVista());

        assertThat(permanent.isTapped()).isFalse();
    }

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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CanopyVista());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent findCanopyVista(Player player) {
        return findPermanent(player, "Canopy Vista");
    }
}
