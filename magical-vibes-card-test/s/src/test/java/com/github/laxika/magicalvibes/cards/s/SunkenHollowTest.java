package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunkenHollow.class, Island.class, Swamp.class, DrownedCatacomb.class})
class SunkenHollowTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Island());

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlueMana() {
        addReadySunkenHollow(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlackMana() {
        addReadySunkenHollow(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void entersTappedWithoutAnyLands() {
        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isTrue();
    }

    @Test
    void tappedBasicLandsOfTheSameTypeCount() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithMoreThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        playSunkenHollow();

        assertThat(findSunkenHollow(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandWithBasicLandTypesDoesNotSupplySecondBasicLand() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new SunkenHollow());

        playSunkenHollow();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(2).isTapped()).isTrue();
    }

    private void playSunkenHollow() {
        harness.setHand(player1, List.of(new SunkenHollow()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySunkenHollow(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SunkenHollow());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent findSunkenHollow(Player player) {
        return findPermanent(player, "Sunken Hollow");
    }
}
